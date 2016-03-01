package rest;

import main.AccountService;
import main.RestApplication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Date;

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
    public Response loginUser(String jsonString, @Context HttpHeaders headers){
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

        if (accountService.getUser(login) != null && accountService.getUser(login).getPassword().equals(password)){
            UserProfile user = accountService.getUser(login);

            Cookie cookie = new Cookie(RestApplication.SESSION_COOKIE_NAME, Integer.toUnsignedString((user.getLogin()+user.getPassword()).hashCode(), 9), "/api", null, 1);
            NewCookie cookie1 = new NewCookie(cookie, null, NewCookie.DEFAULT_MAX_AGE, true);

            accountService.loginUser(cookie, user.getId());
            return Response.ok(new JSONObject().put("id", accountService.getUser(login).getId()).toString()).cookie(cookie1).build();
        }
        else
            return WebErrorManager.authorizationRequired();

    }

    // Read
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response isAuthenticated(@Context HttpHeaders headers) {
        Cookie cookie = headers.getCookies().get(RestApplication.SESSION_COOKIE_NAME);
        if (cookie == null)
            return WebErrorManager.authorizationRequired();

        return Response.ok(new JSONObject().put("id", accountService.getByCookie(cookie).getId()).toString()).build();
    }

    // Delete
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@Context HttpHeaders headers){
        Cookie cookie = headers.getCookies().get(RestApplication.SESSION_COOKIE_NAME);
        if (cookie != null)
        {
            accountService.logoutUser(cookie);

            NewCookie cookie1 = new NewCookie(RestApplication.SESSION_COOKIE_NAME, null, null, null, 0, "DELETED", 0, true);    // As full as possible!
            return Response.ok(new JSONObject().toString()).cookie(cookie1).build();
        }

        return Response.ok(new JSONObject().toString()).build();
    }



    private AccountService accountService;
}
