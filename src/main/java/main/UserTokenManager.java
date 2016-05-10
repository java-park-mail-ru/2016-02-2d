package main;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class UserTokenManager {

    @NotNull
    public static String getNewRandomSessionID(Object... stringConvertibles) {
        final StringBuilder plainID = new StringBuilder(SALT);
        for (Object object : stringConvertibles)
            plainID.append(object.toString());

        return Base64.getEncoder().encodeToString(plainID.toString().getBytes(StandardCharsets.UTF_8));
    }

    @NotNull
    public static NewCookie getNewCookieWithSessionID(String sessionID) {
        return getNewCookie(sessionID, cookieMaxAge);
    }

    @NotNull
    public static NewCookie getNewNullCookie() {
        return getNewCookie(null, 0);
    }

    @Nullable
    public static String getSIDStringFromHeaders(HttpHeaders headers) {
        if (headers.getCookies().containsKey(UserTokenManager.COOKIE_NAME))
            return headers.getCookies().get(COOKIE_NAME).getValue();
        else return null;
    }

    public static void changeHost(@Nullable String newHost) {
        if (newHost != null)
            host = newHost;
    }

    public static void changeMaxAge(int seconds) {
        if (seconds > 0)
            cookieMaxAge = seconds;
    }

    private static NewCookie getNewCookie(@Nullable String sessionID, int maxAge) {
        String sessionID1 = sessionID;
        if (sessionID1 == null)
            sessionID1 = "";
        return new NewCookie(COOKIE_NAME, sessionID1, "/", host, "This cookie is used to authenticate users in the Bomberman game.", maxAge, false);
    }

    public static final String COOKIE_NAME = "BOMBERMAN-SESSION-TOKEN";

    private static final int COOKIE_MAX_AGE_SECONDS = 3660; // 1 hour
    private static final String SALT = "Just some words.";

    private static String host = "localhost";
    private static int cookieMaxAge = COOKIE_MAX_AGE_SECONDS;


}
