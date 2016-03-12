package rest;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.core.Response;

// All these codes are not magic numbers. They are specified in HTTP specifications! (I wish Java had C-styled enums... E.g. Response.Status.BAD_REQUEST == 400)
@SuppressWarnings("MagicNumber")
public class WebErrorManager {

    public static Response badJSON(){
        return badRequest("Request was not a valid JSON!");
    }

    public static Response badRequest(@Nullable String reason){
        return Response.status(Response.Status.BAD_REQUEST).entity(new JSONObject().put("status", 400).put("message", reason).toString()).build();
    }

    public static Response accessForbidden()
    {
        return Response.status(Response.Status.FORBIDDEN).entity(new JSONObject().put("status", 403).toString()).build();
    }

    public static Response accessForbidden(@Nullable String reason){
        if (reason == null || reason.isEmpty())
            return accessForbidden();
        return Response.status(Response.Status.FORBIDDEN).entity(new JSONObject().put("status", 403).put("message", reason).toString()).build();
    }

    public static Response accessForbidden(@Nullable JSONArray reason){
        if (reason == null)
            return accessForbidden();
        return Response.status(Response.Status.FORBIDDEN).entity(new JSONObject().put("status", 403).put("message", reason).toString()).build();
    }

    public static Response authorizationRequired()
    {
        return Response.status(Response.Status.UNAUTHORIZED).entity(new JSONObject().put("status", 401).toString()).build();
    }

    public static Response authorizationRequired(@Nullable String reason){
        if (reason == null || reason.isEmpty())
            return authorizationRequired();
        return Response.status(Response.Status.UNAUTHORIZED).entity(new JSONObject().put("status", 401).put("message", reason).toString()).build();
    }

    public static Response serverError()
    {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new JSONObject().put("status", 500).put("message", "Something went wrong, but it was expected.\nI hope this soothe you.\n\n:(").toString()).build();
    }

    public static Response serverError(@Nullable String reason){
        if (reason == null || reason.isEmpty())
            return serverError();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new JSONObject().put("status", 500).put("message", reason).toString()).build();
    }

    public static Response ok()
    {
        return Response.status(Response.Status.OK).entity(new JSONObject().put("status", 200).toString()).build();
    }

    public static Response ok(@Nullable String reason){
        if (reason == null || reason.isEmpty())
            return ok();
        return Response.status(Response.Status.OK).entity(new JSONObject().put("status", 200).put("message", reason).toString()).build();
    }

    // Hack
    public static Response.ResponseBuilder okRaw(@Nullable String reason){
        if (reason == null || reason.isEmpty())
            return Response.ok();
        return Response.status(Response.Status.OK).entity(new JSONObject().put("status", 200).put("message", reason).toString());
    }

    @Nullable
    public static JSONArray showFieldsNotPresent(@NotNull JSONObject json,@NotNull String[] requiredFields){
        JSONArray errorList = null;
        for (String field : requiredFields)
            if (!json.has(field)){
                if (errorList == null)
                    errorList = new JSONArray();
                errorList.put(new JSONObject().put(field, "Field \"" + field + "\" not present!"));
            }

        return errorList;
    }
}

