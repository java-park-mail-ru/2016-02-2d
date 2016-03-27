package rest;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.core.Response;

public class WebErrorManager {

    public static Response badJSON(){
        return badRequest("Request was not a valid JSON!");
    }

    @SuppressWarnings("unused")
    public static Response badRequest() {
        return badRequest(null);
    }

    public static Response badRequest(@Nullable Object reason){
        return anyResponse(Response.Status.BAD_REQUEST, reason).build();
    }

    public static Response accessForbidden()
    {
        return accessForbidden(null);
    }

    public static Response accessForbidden(@Nullable Object reason){
        return anyResponse(Response.Status.FORBIDDEN, reason).build();
    }

    public static Response authorizationRequired()    {
        return authorizationRequired(null);
    }

    public static Response authorizationRequired(@Nullable Object reason){
        return anyResponse(Response.Status.UNAUTHORIZED, reason).build();
    }

    @SuppressWarnings("unused")
    public static Response serverError()    {
        return serverError("Something went wrong, but it was expected.\nI hope this soothe you.\n\n:(");
    }

    public static Response serverError(@Nullable Object reason){
        return anyResponse(Response.Status.INTERNAL_SERVER_ERROR, reason).build();
    }

    @SuppressWarnings("StaticMethodNamingConvention")
    public static Response ok()
    {
        return ok(null);
    }

    @SuppressWarnings("StaticMethodNamingConvention")
    public static Response ok(@Nullable String reason){
        return anyResponse(Response.Status.OK, reason).build();
    }

    public static Response.ResponseBuilder anyResponse(@NotNull Response.Status code, @Nullable Object reason) {
        final JSONObject response = new JSONObject().put("status", code.getStatusCode());
        if (reason != null)
            response.put("message", reason);
        return Response.status(code).entity(response.toString());
    }

    public static Response.ResponseBuilder okRaw(@Nullable String reason){
        return anyResponse(Response.Status.OK, reason);
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

