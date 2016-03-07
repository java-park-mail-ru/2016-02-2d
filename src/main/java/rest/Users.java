package rest;

import main.AccountService;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

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
        final JSONObject jsonRequest;

        try {
            jsonRequest = new JSONObject(jsonString);
        } catch (JSONException ex) {
            return WebErrorManager.badJSON();
        }

        final String login;
        final String password;
        final JSONArray errorList = WebErrorManager.showFieldsNotPresent(jsonRequest, new String[]{"login","password"});
        if (errorList == null){
            login = jsonRequest.get("login").toString();
            password = jsonRequest.get("password").toString();
        }
        else
            return WebErrorManager.accessForbidden(errorList);

        final UserProfile newUser = accountService.createNewUser(login, password);
        if (newUser != null)
            return Response.ok(new JSONObject().put("id", newUser.getId()).toString()).build();
        else
            return WebErrorManager.accessForbidden("User already exists!");

    }

    // Read
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        final JSONArray responseJSON = new JSONArray();

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
    public Response updateUser(String jsonString, @Context HttpServletRequest request){
        if (request.isSecure())
        {
            final JSONObject jsonRequest;

            try {
                jsonRequest = new JSONObject(jsonString);
            } catch (JSONException ex) {
                return WebErrorManager.badJSON();
            }

            final String login;
            final String password;
            final JSONArray errorList = WebErrorManager.showFieldsNotPresent(jsonRequest, new String[]{"login","password"});
            if (errorList == null){
                login = jsonRequest.get("login").toString();
                password = jsonRequest.get("password").toString();
            }
            else
                return WebErrorManager.accessForbidden(errorList);


            final UserProfile user = accountService.getUser(login);
            if (user != null && user.getPassword().equals(password)){
                // TODO: Updating here.
                return Response.ok(new JSONObject().put("id", user.getId()).toString()).build();
            }
            else
                return WebErrorManager.authorizationRequired("Wrong login-password pair!");

        } else
            return WebErrorManager.accessForbidden("Not your user!");
    }

    // Delete
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("id") Long id, @Context HttpServletRequest request){
        if (request.isSecure())
        {
            accountService.deleteUser(id);
            return Response.ok(new JSONObject().toString()).build();
        } else
            return WebErrorManager.authorizationRequired();
    }

    private final AccountService accountService;
}
