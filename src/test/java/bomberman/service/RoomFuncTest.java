package bomberman.service;

import bomberman.mechanics.Bomberman;
import main.UserTokenManager;
import main.accountservice.AccountService;
import main.accountservice.AccountServiceImpl;
import main.config.Context;
import main.databaseservice.DataBaseService;
import main.databaseservice.DataBaseServiceHashMapImpl;
import main.websocketconnection.WebSocketConnection;
import main.websocketconnection.WebSocketConnectionCreator;
import java.net.HttpCookie;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.javatuples.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Test;
import rest.UserProfile;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RoomFuncTest {

    @Test
    public void setupApplication() {
        try {
            final DataBaseService db = new DataBaseServiceHashMapImpl();
            final RoomManager roomManager = new RoomManagerImpl();
            context.put(DataBaseService.class, db);
            context.put(AccountService.class, new AccountServiceImpl(db));
            context.put(RoomManager.class, roomManager);

            final WebSocketConnectionCreator servlet = new WebSocketConnectionCreator(context);

            for (UserProfile user : createUsers()) {
                final ServletUpgradeRequest mockedRequest = mock(ServletUpgradeRequest.class);
                final List<HttpCookie> oneCookieList = new LinkedList<>();
                oneCookieList.add(new HttpCookie(UserTokenManager.COOKIE_NAME, user.getSessionID()));
                when(mockedRequest.getCookies()).thenReturn(oneCookieList);

                final WebSocketConnection connection = (WebSocketConnection) servlet.createWebSocket(mockedRequest, mock(ServletUpgradeResponse.class));

                assertNotNull(connection);

                final Session session = mock(WebSocketSession.class);
                final RemoteEndpoint remote = mock(RemoteEndpoint.class);
                when(session.getRemote()).thenReturn(remote);
                doAnswer((invocationOnMock) -> {handleMessages((String)invocationOnMock.getArguments()[0]); return null;}).when(remote).sendString(anyString());

                connection.onOpen(session);
            }

            room = roomManager.getAllRooms().get(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @AfterClass
    public static void wasUserJoinedAndBroadcastTested() {
        assertEquals(true, wasUserJoinedTested);    // room.broadcast() was tested here too.
    }

    private void handleMessages(String message) throws JSONException {
        assertNotNull(message);
        final JSONObject jsonnedMessage;
        try {
            jsonnedMessage = new JSONObject(message);
        } catch (JSONException ex) {
            ex.printStackTrace();   // Should never happen. Messages are generated through "new JSONObject"... :)
            throw ex;
        }

        if (jsonnedMessage.has("type") && jsonnedMessage.getString("type").equals("user_joined"))
            testUserJoined(jsonnedMessage);
    }

    private Set<UserProfile> createUsers() {
        final AccountService accountService = (AccountService) context.get(AccountService.class);
        final Set<UserProfile> result = new HashSet<>();

        for (int i = 0; i < 4; ++i) {
            final UserProfile user = accountService.createNewUser("user_" + i, "my_id_is_" + i);
            assertNotNull(user);
            result.add(user);
            accountService.loginUser(user);
            users.add(user);
        }

        return result;
    }

    private void testUserJoined(JSONObject message) {
        amountOfJoinedBroadcasts++;
        boolean isAnExistingUser = false;

        for (UserProfile entry: users)
            if (entry.getLogin().equals(message.get("name"))) {
                isAnExistingUser = true;
                usersBroadcasted.add(entry);
            }

        assertEquals(true, isAnExistingUser);

        // times(16)
        if (amountOfJoinedBroadcasts == AMOUNT_OF_JOINED_BROADCASTS && usersBroadcasted.size() == users.size())
            wasUserJoinedTested = true;
        if (amountOfJoinedBroadcasts > AMOUNT_OF_JOINED_BROADCASTS)
            wasUserJoinedTested = false;
    }

    private final Context context = new Context();
    private Room room = null;

    private static boolean wasUserJoinedTested = false;
    private List<UserProfile> users = new LinkedList<>();
    private Set<UserProfile> usersBroadcasted = new HashSet<>();
    private static final int AMOUNT_OF_JOINED_BROADCASTS = 16;  // 1 user — tell him he is accepted. 2 users — send data to #1 about #2 and #2 about #1 plus tell #2  he is joined. 3 users: #1↔#3 and #2↔#3 plus #3→#3, while #1 and #2 already know about themselves.
    private int amountOfJoinedBroadcasts = 0;                   // So, number of broadcast on new user joined is (prev_user_amnt)*2 + 1. And f(1) + f(2) + f(3) + f(4) = 1 + 3 + 5 + 7 = 16
}
