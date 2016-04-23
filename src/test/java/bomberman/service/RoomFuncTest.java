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
import org.junit.*;
import rest.UserProfile;
import rest.WebErrorManager;

import java.sql.Connection;
import java.util.*;

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
        assertEquals(true, joinedBroadcasts.isPassed());    // room.broadcast() was tested here too.
        casesSuccesfullyTested++;
        System.out.println("[" + casesSuccesfullyTested + '/' + TOTAL_CASES_TO_TEST + "] user_joined was tested!");
    }

    @After
    public void wasUserLeftTested() {
        assertEquals(true, leftBroadcasts.isPassed());
        casesSuccesfullyTested++;
        System.out.println("[" + casesSuccesfullyTested + '/' + TOTAL_CASES_TO_TEST + "] user_left was tested!");
    }

    @After
    public void wasUserReadyTested() {
        assertEquals(true, readyBroadcasts.isPassed());
        casesSuccesfullyTested++;
        System.out.println("[" + casesSuccesfullyTested + '/' + TOTAL_CASES_TO_TEST + "] user_state_changed was tested!");
    }

    @BeforeClass
    public static void defineTests() {

    }

    private void run() {
        logUsersOut();
        makeUsersReady();
    }

    private void logUsersOut() {
        for (Pair<UserProfile, WebSocketConnection> entry: users) {
            entry.getValue1().onClose(0, "Hate \"Magic numbers\" isnpections");
            ((AccountService) context.get(AccountService.class)).logoutUser(entry.getValue0().getSessionID());
        }
    }

    private void makeUsersReady() {
        for (Pair<UserProfile, WebSocketConnection> entry : users) {
            final JSONObject message = new JSONObject().put("type", "user_state_changed").put("id", entry.getValue0().getId())
                    .put("isReady", true).put("contentLoaded", false);
            entry.getValue1().onMessage(message.toString());
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
            joinedBroadcasts.count(jsonnedMessage);
        if (jsonnedMessage.getString("type").equals("user_left"))
            leftBroadcasts.count(jsonnedMessage);
        if (jsonnedMessage.getString("type").equals("user_state_changed") && !jsonnedMessage.getBoolean("contentLoaded"))
            readyBroadcasts.count(jsonnedMessage);
//      if (jsonnedMessage.getString("type").equals("user_state_changed") && jsonnedMessage.getBoolean("contentLoaded"))
//            readyBroadcasts.count(jsonnedMessage);
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


    private static class BroadcastsCounter {
        BroadcastsCounter(int totalBroadcasts, int userMentions) {
            totalAmountOfBroadcasts = totalBroadcasts;
            usersToBeMentioned = userMentions;
        }

        public void count(JSONObject message) {
            currentAmountOfBroadcasts++;
            boolean isAnExistingUser = false;

            for (Pair<UserProfile, WebSocketConnection> entry: users)
                if (entry.getValue0().getId() == message.getInt("id")) {
                    isAnExistingUser = true;
                    usersMentioned.add(entry.getValue0());
                }

            assertEquals(true, isAnExistingUser);

            if (currentAmountOfBroadcasts == totalAmountOfBroadcasts && usersMentioned.size() == usersToBeMentioned)
                isPassed = true;
            if (currentAmountOfBroadcasts > totalAmountOfBroadcasts)
                isPassed = false;
        }

        public boolean isPassed() {return isPassed;}

        private final int totalAmountOfBroadcasts;
        private final int usersToBeMentioned;

        private int currentAmountOfBroadcasts = 0;
        private boolean isPassed = false;
        private Set<UserProfile> usersMentioned = new HashSet<>();
    }

    private final Context context = new Context();
    private Room room = null;
    private static List<Pair<UserProfile, WebSocketConnection>> users = new LinkedList<>();

    private static final int AMOUNT_OF_JOINED_BROADCASTS = 16;  // 1 user — tell him he is accepted. 2 users — send data to #1 about #2 and #2 about #1 plus tell #2  he is joined. 3 users: #1↔#3 and #2↔#3 plus #3→#3, while #1 and #2 already know about themselves.
    private static BroadcastsCounter joinedBroadcasts = new BroadcastsCounter(AMOUNT_OF_JOINED_BROADCASTS, 4); // So, number of broadcast on new user joined is (prev_user_amnt)*2 + 1. And f(1) + f(2) + f(3) + f(4) = 1 + 3 + 5 + 7 = 16. Or simply new_user_amt^2

    private static final int AMOUNT_OF_LEFT_BROADCASTS = 6;  // 3! transmissions for 4 users
    private static BroadcastsCounter leftBroadcasts = new BroadcastsCounter(AMOUNT_OF_LEFT_BROADCASTS, 3);

    private static final int AMOUNT_OF_USER_READY_BROADCASTS = 16;  // 4^2 transmissions for 4 users
    private static BroadcastsCounter readyBroadcasts = new BroadcastsCounter(AMOUNT_OF_USER_READY_BROADCASTS, 4);

    private static final int TOTAL_CASES_TO_TEST = 3;
    private int casesSuccesfullyTested = 0;

}
