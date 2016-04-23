package bomberman.service;

import bomberman.mechanics.Bomberman;
import constants.Constants;
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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import rest.UserProfile;
import rest.WebErrorManager;

import java.sql.Connection;
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
    @Ignore
    public void setupApplication() {
        try {
            final DataBaseService db = new DataBaseServiceHashMapImpl();
            final RoomManager roomManager = new RoomManagerImpl();
            context.put(DataBaseService.class, db);
            context.put(AccountService.class, new AccountServiceImpl(db));
            context.put(RoomManager.class, roomManager);

            final WebSocketConnectionCreator servlet = new WebSocketConnectionCreator(context);

            for (UserProfile user : createUsers()) {
                final WebSocketConnection connection = Constants.GameMechanicsMocks.createMockedConnection(user, servlet);
                final Session session = Constants.GameMechanicsMocks.createMockedSession(
                        (invocationOnMock) -> {handleMessages((String)invocationOnMock.getArguments()[0]); return null;}
                );

                users.add(new Pair<>(user, connection));
                connection.onOpen(session);
            }

            room = roomManager.getAllRooms().get(0);

            run();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @After
    public void wasUserJoinedAndBroadcastTested() {
        assertEquals(true, wasUserJoinedTested);    // room.broadcast() was tested here too.
        casesSuccesfullyTested++;
        System.out.println("[" + casesSuccesfullyTested + '/' + TOTAL_CASES_TO_TEST + "] user_joined was tested!");
    }

    @After
    public void wasUserLeftTested() {
        assertEquals(true, wasUserLeftTested);
        casesSuccesfullyTested++;
        System.out.println("[" + casesSuccesfullyTested + '/' + TOTAL_CASES_TO_TEST + "] user_left was tested!");
    }

    private void run() {
        logUsersOut();
    }

    private void logUsersOut() {
        for (Pair<UserProfile, WebSocketConnection> entry: users) {
            entry.getValue1().onClose(0, "Hate \"Magic numbers\" isnpections");
            ((AccountService) context.get(AccountService.class)).logoutUser(entry.getValue0().getSessionID());
        }
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

        assertNull(WebErrorManager.showFieldsNotPresent(jsonnedMessage, "type"));

        if (jsonnedMessage.getString("type").equals("user_joined"))
            testUserJoined(jsonnedMessage);
        if (jsonnedMessage.getString("type").equals("user_left"))
            testUserLeft(jsonnedMessage);
    }

    private Set<UserProfile> createUsers() {
        final AccountService accountService = (AccountService) context.get(AccountService.class);
        final Set<UserProfile> result = new HashSet<>();

        for (int i = 0; i < 4; ++i) {
            final UserProfile user = accountService.createNewUser("user_" + i, "my_id_is_" + i);
            assertNotNull(user);
            result.add(user);
            accountService.loginUser(user);
        }

        return result;
    }

    private void testUserJoined(JSONObject message) {
        amountOfJoinedBroadcasts++;
        boolean isAnExistingUser = false;

        for (Pair<UserProfile, WebSocketConnection> entry: users)
            if (entry.getValue0().getLogin().equals(message.get("name"))) {
                isAnExistingUser = true;
                usersJoinedBroadcasted.add(entry.getValue0());
            }

        assertEquals(true, isAnExistingUser);

        // times(16)
        if (amountOfJoinedBroadcasts == AMOUNT_OF_JOINED_BROADCASTS && usersJoinedBroadcasted.size() == users.size())
            wasUserJoinedTested = true;
        if (amountOfJoinedBroadcasts > AMOUNT_OF_JOINED_BROADCASTS)
            wasUserJoinedTested = false;
    }

    private void testUserLeft(JSONObject message) {
        amountOfLeftBroadcasts++;
        boolean isAnExistingUser = false;

        for (Pair<UserProfile, WebSocketConnection> entry : users)
            if (entry.getValue0().getId() == message.getInt("id")) {
                isAnExistingUser = true;
                usersLeftBroadcasted.add(entry.getValue0());
            }

        assertEquals(true, isAnExistingUser);

        if (amountOfLeftBroadcasts == AMOUNT_OF_LEFT_BROADCASTS && usersLeftBroadcasted.size() == users.size() - 1)
            wasUserLeftTested = true;
        if (amountOfLeftBroadcasts > AMOUNT_OF_LEFT_BROADCASTS)
            wasUserLeftTested = false;
    }

    private final Context context = new Context();
    private Room room = null;
    private List<Pair<UserProfile, WebSocketConnection>> users = new LinkedList<>();

    private boolean wasUserJoinedTested = false;
    private Set<UserProfile> usersJoinedBroadcasted = new HashSet<>();
    private static final int AMOUNT_OF_JOINED_BROADCASTS = 16;  // 1 user — tell him he is accepted. 2 users — send data to #1 about #2 and #2 about #1 plus tell #2  he is joined. 3 users: #1↔#3 and #2↔#3 plus #3→#3, while #1 and #2 already know about themselves.
    private int amountOfJoinedBroadcasts = 0;                   // So, number of broadcast on new user joined is (prev_user_amnt)*2 + 1. And f(1) + f(2) + f(3) + f(4) = 1 + 3 + 5 + 7 = 16. Or simply new_user_amt^2

    private boolean wasUserLeftTested = false;
    private Set<UserProfile> usersLeftBroadcasted = new HashSet<>();
    private static final int AMOUNT_OF_LEFT_BROADCASTS = 6;  // 3! transmissions for 4 users
    private int amountOfLeftBroadcasts = 0;

    private static final int TOTAL_CASES_TO_TEST = 2;
    private int casesSuccesfullyTested = 0;

}
