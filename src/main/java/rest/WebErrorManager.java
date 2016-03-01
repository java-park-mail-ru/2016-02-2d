package rest;

import org.json.JSONObject;

import javax.ws.rs.core.Response;

// All these codes are not magic numbers. They are specified in HTTP specifications! (I wish Java had C-styled enums... E.g. Response.Status.BAD_REQUEST == 400)
@SuppressWarnings("MagicNumber")
public class WebErrorManager {

    public static Response badJSON(){
        return badRequest("Request was not a valid JSON!");
    }

    public static Response badRequest(String reason){
        return Response.status(Response.Status.BAD_REQUEST).entity(new JSONObject().put("status", 400).put("message", reason).toString()).build();
    }

    public static Response accessForbidden()
    {
        return Response.status(Response.Status.FORBIDDEN).entity(new JSONObject().toString()).build();
    }

    @SuppressWarnings("SameParameterValue") // Will change in future.
    public static Response accessForbidden(String reason){
        if (reason == null || reason.isEmpty())
            return accessForbidden();
        return Response.status(Response.Status.FORBIDDEN).entity(new JSONObject().put("status", 403).put("message", "Not your user!").toString()).build();
    }

    public static Response authorizationRequired()
    {
        return Response.status(Response.Status.UNAUTHORIZED).entity(new JSONObject().toString()).build();
    }

    @SuppressWarnings("SameParameterValue") // Probably will change in future.
    public static Response authorizationRequired(String reason){
        if (reason == null || reason.isEmpty())
            return authorizationRequired();
        return Response.status(Response.Status.UNAUTHORIZED).entity(new JSONObject().put("status", 401).put("message",reason).toString()).build();
    }
}
