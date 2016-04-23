package constants;

import bomberman.mechanics.Bomberman;
import bomberman.mechanics.interfaces.EventObtainable;
import bomberman.mechanics.interfaces.EventStashable;
import bomberman.mechanics.interfaces.UniqueIDManager;
import main.accountservice.AccountService;
import main.accountservice.AccountServiceImpl;
import main.UserTokenManager;
import main.websocketconnection.MessageSendable;
import main.websocketconnection.WebSocketConnection;
import main.websocketconnection.WebSocketConnectionCreator;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.json.JSONObject;
import org.mockito.stubbing.Answer;
import rest.UserProfile;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Constants {

    public static class RestApplicationMocks {

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
                okCookieMap.put(UserTokenManager.COOKIE_NAME, UserTokenManager.getNewCookieWithSessionID(USER_SESSION_ID));
                final Map<String, Cookie> badCookieMap = new HashMap<>();
                badCookieMap.put(UserTokenManager.COOKIE_NAME, UserTokenManager.getNewCookieWithSessionID("ERRONEOUS DATA"));

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

    public static class GameMechanicsMocks {

        public static WebSocketConnection createMockedConnection(UserProfile user, WebSocketConnectionCreator servlet) {
            final ServletUpgradeRequest mockedRequest = mock(ServletUpgradeRequest.class);
            final List<HttpCookie> oneCookieList = new LinkedList<>();
            oneCookieList.add(new HttpCookie(UserTokenManager.COOKIE_NAME, user.getSessionID()));
            when(mockedRequest.getCookies()).thenReturn(oneCookieList);

            final WebSocketConnection connection = (WebSocketConnection) servlet.createWebSocket(mockedRequest, mock(ServletUpgradeResponse.class));
            assertNotNull(connection);

            return connection;
        }

        public static Session createMockedSession(Answer messageHandler) throws Exception {
            final Session session = mock(WebSocketSession.class);
            final RemoteEndpoint remote = mock(RemoteEndpoint.class);
            when(session.getRemote()).thenReturn(remote);
            doAnswer(messageHandler).when(remote).sendString(anyString());

            return session;
        }

        public static MessageSendable uniqueMockMessageSendable() {
            return mock(MessageSendable.class);
        }

        public static UniqueIDManager getUniqueIDManager() {
            configure();
            return uniqueIDManager;
        }

        public static EventStashable getEventStashable() {
            configure();
            return EVENT_STASHABLE;
        }

        public static Bomberman getBomberman() {
            configure();
            return BOMBERMAN;
        }

        private static void configure() {
            if (!isConfigured) {

                uniqueIDManager = new UniqueIDManager() {
                    @Override
                    public int getNextID() {
                        return mockedIDGenerator.getAndIncrement();
                    }

                    private AtomicInteger mockedIDGenerator = new AtomicInteger();
                };

                when(BOMBERMAN.getID()).thenReturn((int) USER_ID);

                isConfigured = true;
            }
        }

        private static UniqueIDManager uniqueIDManager = null;
        private static final EventStashable EVENT_STASHABLE = mock(EventStashable.class);
        private static final Bomberman BOMBERMAN = mock(Bomberman.class);

        private static boolean isConfigured = false;
    }

    public static UserProfile customMockUserProfile(String login, String password, String sessionID, Integer score) {
        final UserProfile mocked = mock(UserProfile.class);

        final long id = RANDOM_ID_GENERATOR.getAndIncrement();
        when(mocked.getId()).thenReturn(id);
        when(mocked.getLogin()).thenReturn(login);
        when(mocked.getPassword()).thenReturn(password);
        when(mocked.getSessionID()).thenReturn(sessionID);
        when(mocked.getScore()).thenReturn(score);
        when(mocked.toJson()).thenReturn(new JSONObject().put("id", id).put("login", login).put("score", score));

        return mocked;
    }

    public static final String USER_LOGIN = "login";
    public static final String USER_PASSWORD = "password";
    public static final long USER_ID = 0xDEADBEEF;
    public static final String USER_SESSION_ID = "TEST_SESSION_ID";

    private static final AtomicLong RANDOM_ID_GENERATOR = new AtomicLong(1);
}
