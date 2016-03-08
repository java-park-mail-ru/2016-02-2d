package rest;

import main.AccountService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Singleton
@Path("/session")
public class Sessions {

    public Sessions(AccountService accountService) {
        this.accountService = accountService;
    }

    // Create
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(String jsonString, @Context HttpServletRequest request){
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

        UserProfile loggingUser = accountService.getUser(login);
        if (loggingUser != null) {
            if (loggingUser.getPassword().equals(password))
            {
                accountService.loginUser(request.getSession().getId(), loggingUser.getId());
                return Response.ok(new JSONObject().put("id", loggingUser.getId()).toString()).build();
            }
            else
                return WebErrorManager.authorizationRequired("Wrong login-password pair!");
        }
        else
            return WebErrorManager.authorizationRequired("Wrong login!");

    }

    // Read
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response isAuthenticated(@Context HttpServletRequest request) {

            UserProfile currentUser = accountService.getBySessionID(request.getSession().getId());
            if (currentUser != null)
                return Response.ok(new JSONObject().put("id",currentUser.getId()).toString()).build();
            else return WebErrorManager.serverError("Session exists, but no user is assigned to.");

    }

    // Delete
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response logoutUser(@Context HttpServletRequest request){
        String sessionID = request.getSession().getId();

        request.getSession().invalidate();
        if (accountService.logoutUser(sessionID))
            return WebErrorManager.ok("You have succesfully logged out.");
        else
            return WebErrorManager.ok("You was not logged in.");
    }



    private final AccountService accountService;
}
