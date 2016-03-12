package rest;

import main.AccountService;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import main.TokenManager;
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
    public Response createUser(String jsonString, @Context HttpHeaders headers){
        accountService.logoutUser(TokenManager.getSIDStringFromHeaders(headers));

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
        if (newUser != null) {
            accountService.loginUser(newUser);
            return WebErrorManager.okRaw(new JSONObject().put("id", newUser.getId()).toString()).cookie(TokenManager.getNewCookieWithSessionID(newUser.getSessionID())).build();
        }
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
    public Response updateUser(String jsonString, @Context HttpHeaders headers){
        if (accountService.hasSessionID(TokenManager.getSIDStringFromHeaders(headers)))
        {
            final JSONObject jsonRequest;

            try {
                jsonRequest = new JSONObject(jsonString);
            } catch (JSONException ex) {
                return WebErrorManager.badJSON();
            }

            final String login;
            final JSONArray errorList = WebErrorManager.showFieldsNotPresent(jsonRequest, new String[]{"login"});
            if (errorList == null){
                login = jsonRequest.get("login").toString();
            }
            else
                return WebErrorManager.accessForbidden(errorList);


            final UserProfile user = accountService.getUser(login);
            if (user != null){
                final String password = jsonRequest.get("password").toString();
                if (password != null)
                    user.setPassword(password);

                return WebErrorManager.ok(new JSONObject().put("id", user.getId()).toString());
            }
            else
                return WebErrorManager.serverError("Session exists, but user does not!");

        } else
            return WebErrorManager.authorizationRequired("Not logged in!");
    }

    // Delete
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("id") Long id, @Context HttpHeaders headers){
        if (accountService.hasSessionID(TokenManager.getSIDStringFromHeaders(headers)))
        {
            UserProfile supplicant = accountService.getBySessionID(TokenManager.getSIDStringFromHeaders(headers));
            if (supplicant == null)
                return WebErrorManager.serverError("Session exists, but user does not!");
            if (supplicant.getId() == id) {
                accountService.logoutUser(supplicant.getSessionID());
                accountService.deleteUser(id);
                return WebErrorManager.ok();
            }
            else return WebErrorManager.accessForbidden("Not your user!");

        } else
            return WebErrorManager.authorizationRequired("Not logged in!");
    }


    private final AccountService accountService;
}
