package constants;

import main.AccountService;
import main.AccountServiceImpl;
import main.TokenManager;
import org.json.JSONObject;
import rest.UserProfile;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Constants {

    public static class FunctionalTestMocks {

        public static AccountService getMockedAccountService() {
            configure();
            return MOCKED_ACCOUNT_SERVICE;
        }

        public static UserProfile getMockedUserProfile() {
            configure();
            return MOCKED_USER_PROFILE;
        }

        public static HttpHeaders getNoCookieHeaders() {
            configure();
            return NO_COOKIE_HEADERS;
        }

        public static HttpHeaders getOkCookieHeaders() {
            configure();
            return OK_COOKIE_HEADERS;
        }

        public static HttpHeaders getWrongCookieHeaders() {
            configure();
            return WRONG_COOKIE_HEADERS;
        }

        private static void configure() {
            if (!isConfigured) {
                when(MOCKED_ACCOUNT_SERVICE.getUser(USER_LOGIN)).thenReturn(MOCKED_USER_PROFILE);
                when(MOCKED_ACCOUNT_SERVICE.getUser(USER_ID)).thenReturn(MOCKED_USER_PROFILE);
                when(MOCKED_ACCOUNT_SERVICE.getBySessionID(USER_SESSION_ID)).thenReturn(MOCKED_USER_PROFILE);
                when(MOCKED_ACCOUNT_SERVICE.hasSessionID(USER_SESSION_ID)).thenReturn(true);
                when(MOCKED_ACCOUNT_SERVICE.logoutUser(USER_SESSION_ID)).thenReturn(true);
                when(MOCKED_ACCOUNT_SERVICE.createNewUser(USER_LOGIN, USER_PASSWORD)).thenReturn(MOCKED_USER_PROFILE);
                when(MOCKED_ACCOUNT_SERVICE.createNewUser(USER_PASSWORD, USER_LOGIN)).thenReturn(null);

                when(MOCKED_USER_PROFILE.toJson()).thenReturn(new JSONObject().put("id", USER_ID).put("login", USER_LOGIN).put("score", 0L));
                when(MOCKED_USER_PROFILE.getId()).thenReturn(USER_ID);
                when(MOCKED_USER_PROFILE.getLogin()).thenReturn(USER_LOGIN);
                when(MOCKED_USER_PROFILE.getPassword()).thenReturn(USER_PASSWORD);
                when(MOCKED_USER_PROFILE.getSessionID()).thenReturn(USER_SESSION_ID);

                final Map<String, Cookie> noCookieMap = new HashMap<>();
                final Map<String, Cookie> okCookieMap = new HashMap<>();
                okCookieMap.put(TokenManager.COOKIE_NAME, TokenManager.getNewCookieWithSessionID(USER_SESSION_ID));
                final Map<String, Cookie> badCookieMap = new HashMap<>();
                badCookieMap.put(TokenManager.COOKIE_NAME, TokenManager.getNewCookieWithSessionID("ERRONEOUS DATA"));

                when(NO_COOKIE_HEADERS.getCookies()).thenReturn(noCookieMap);
                when(OK_COOKIE_HEADERS.getCookies()).thenReturn(okCookieMap);
                when(WRONG_COOKIE_HEADERS.getCookies()).thenReturn(badCookieMap);

                isConfigured = true;
            }
        }

        private static final AccountService MOCKED_ACCOUNT_SERVICE = mock(AccountServiceImpl.class);
        private static final UserProfile MOCKED_USER_PROFILE = mock(UserProfile.class);

        private static final HttpHeaders NO_COOKIE_HEADERS = mock(HttpHeaders.class);
        private static final HttpHeaders OK_COOKIE_HEADERS = mock(HttpHeaders.class);
        private static final HttpHeaders WRONG_COOKIE_HEADERS = mock(HttpHeaders.class);

        private static boolean isConfigured = false;

    }

    public static final String USER_LOGIN = "login";
    public static final String USER_PASSWORD = "password";
    public static final long USER_ID = 0xDEADBEEF;
    public static final String USER_SESSION_ID = "TEST_SESSION_ID";
}
