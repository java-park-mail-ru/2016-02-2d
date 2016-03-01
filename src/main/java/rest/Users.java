package rest;

import main.AccountService;

import javax.inject.Singleton;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import main.RestApplication;
import org.json.*;

@Singleton
@Path("/user")
public class Users {

    public Users(AccountService accountService) {
        this.accountService = accountService;
    }

    // Create
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(String jsonString){
        JSONObject jsonRequest;

        try {
            jsonRequest = new JSONObject(jsonString);
        } catch (JSONException ex) {
            return WebErrorManager.badJSON();
        }
        // TODO: Rewrite copy-paste into separate function?
        String login;
        if (jsonRequest.has("login"))
            login = jsonRequest.get("login").toString();
        else
            return WebErrorManager.accessForbidden();  // Here should be clear explanation what went wrong, but API restricts it. O_o

        String password;
        if (jsonRequest.has("password"))
            password = jsonRequest.get("password").toString();
        else
            return WebErrorManager.accessForbidden();

        String email;
        if (jsonRequest.has("email"))
            email = jsonRequest.get("email").toString();
        else
            return WebErrorManager.accessForbidden();

        if (accountService.createNewUser(login, password, email))
            return Response.ok(new JSONObject().put("id", accountService.getUser(login).getId()).toString()).build();
        else
            return WebErrorManager.accessForbidden();

    }

    // Read
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        JSONArray responseJSON = new JSONArray();

        for (UserProfile user : accountService.getAllUsers())
            responseJSON.put(user.toJson());

        return Response.ok(responseJSON.toString()).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserByName(@PathParam("id") Long id) {
        final UserProfile user = accountService.getUser(id);
        if(user == null){
            return WebErrorManager.accessForbidden();
        }else {
            return Response.ok(user.toJson().toString()).build();
        }
    }


    // Update
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(String jsonString, @Context HttpHeaders headers){
        JSONObject jsonRequest;

        try {
            jsonRequest = new JSONObject(jsonString);
        } catch (JSONException ex) {
            return WebErrorManager.badJSON();
        }

        String login;
        if (jsonRequest.has("login"))
            login = jsonRequest.get("login").toString();
        else
            return WebErrorManager.accessForbidden();  // Here should be clear explanation what went wrong, but API restricts it. O_o

        String password;
        if (jsonRequest.has("password"))
            password = jsonRequest.get("password").toString();
        else
            return WebErrorManager.accessForbidden();

        String email;
        if (jsonRequest.has("email"))
            email = jsonRequest.get("email").toString();
        else
            return WebErrorManager.accessForbidden();

        Cookie cookie = headers.getCookies().get(RestApplication.SESSION_COOKIE_NAME);
        if (cookie == null)
            return WebErrorManager.accessForbidden("Not your user!");

        if (accountService.getByCookie(cookie).getLogin().equals(login)) {      // Not sure if great idea to compare by login, yet they are unique.
            UserProfile user = accountService.getUser(login);
            if (password.equals(user.getPassword())){
                user.setPassword(password);
                user.setEmail(email);
                return Response.ok(new JSONObject().put("id", accountService.getUser(login).getId()).toString()).build();
            }
            else
                return WebErrorManager.authorizationRequired("Wrong login-password pair!");
        } else {
            return WebErrorManager.accessForbidden("Not your user!");
        }

    }

    // Delete
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("id") Long id, @Context HttpHeaders headers){
        Cookie cookie = headers.getCookies().get(RestApplication.SESSION_COOKIE_NAME);
        if (cookie == null)
            return WebErrorManager.accessForbidden("Not your user!");

        if(accountService.deleteUser(id)){
            return Response.ok(new JSONObject().toString()).build();
        } else {
            return WebErrorManager.badRequest("No such user. But you have his cookies. Amazing!");
        }
    }



    private AccountService accountService;
}
