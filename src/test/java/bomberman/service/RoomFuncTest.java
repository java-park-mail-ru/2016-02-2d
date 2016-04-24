package bomberman.service;

import constants.Constants;
import main.accountservice.AccountService;
import main.accountservice.AccountServiceImpl;
import main.config.Context;
import main.databaseservice.DataBaseService;
import main.databaseservice.DataBaseServiceHashMapImpl;
import main.websockets.WebSocketConnection;
import main.websockets.WebSocketConnectionCreator;

import org.eclipse.jetty.websocket.api.Session;
import org.javatuples.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
import rest.UserProfile;
import rest.WebErrorManager;

import java.util.*;

import static org.junit.Assert.*;

public class RoomFuncTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    //@Ignore
    public void setupApplication() {
        //noinspection OverlyBroadCatchBlock
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
            room.createNewWorld("non-exisitng-name-for-basic-world");

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

    @After
    public void wasWorldTransmissionTested() {
        assertEquals(true, tileBroadcasts.isPassed());
        casesSuccesfullyTested++;
        System.out.println("[" + casesSuccesfullyTested + '/' + TOTAL_CASES_TO_TEST + "] world creation was tested!");
    }

    @After
    public void wasWorldCreated() {
        assertEquals(true, worldCreatedBroadcasts.isPassed());
        casesSuccesfullyTested++;
        System.out.println("[" + casesSuccesfullyTested + '/' + TOTAL_CASES_TO_TEST + "] world_created was tested!");
    }

    private void run() {
        makeUsersReady();
        makeUsersHaveContentLoaded();
        makeUsersMove();
        // New code here!
        logUsersOut();
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

    private void makeUsersHaveContentLoaded() {
        for (Pair<UserProfile, WebSocketConnection> entry : users) {
            final JSONObject message = new JSONObject().put("type", "user_state_changed").put("id", entry.getValue0().getId())
                    .put("isReady", true).put("contentLoaded", true);
            entry.getValue1().onMessage(message.toString());
        }
    }

    private void makeUsersMove() {
        for (Pair<UserProfile, WebSocketConnection> entry : users) {
            final JSONObject message = new JSONObject().put("type", "object_changed").put("id", entry.getValue0().getId())
                    .put("x", 1).put("y", 1); // move somewhere.
            entry.getValue1().onMessage(message.toString());
        }
    }

    @SuppressWarnings("OverlyComplexMethod")    // This is just a switch("type") workaround.
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
        else if (jsonnedMessage.getString("type").equals("user_left"))
            leftBroadcasts.count(jsonnedMessage);
        else if (jsonnedMessage.getString("type").equals("user_state_changed") && !jsonnedMessage.getBoolean("contentLoaded"))
            readyBroadcasts.count(jsonnedMessage);
        else //noinspection StatementWithEmptyBody
            if (jsonnedMessage.getString("type").equals("user_state_changed") && jsonnedMessage.getBoolean("contentLoaded"))
        {/* ignore */}
        else if (jsonnedMessage.getString("type").equals("object_spawned") && !worldCreatedBroadcasts.isPassed())
            tileBroadcasts.count(jsonnedMessage);
        else if (jsonnedMessage.getString("type").equals("world_created") && !worldCreatedBroadcasts.isPassed())
            worldCreatedBroadcasts.count(jsonnedMessage);
        else
        {
            System.out.println(message);
            //throw new UnsupportedOperationException(); // For messages not implemented yet. :)
        }
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

        protected final int totalAmountOfBroadcasts;
        private final int usersToBeMentioned;

        protected int currentAmountOfBroadcasts = 0;
        protected boolean isPassed = false;
        private Set<UserProfile> usersMentioned = new HashSet<>();
    }

    private static class SimpleBroadcastsCounter extends BroadcastsCounter {
        SimpleBroadcastsCounter(int totalBroadcasts) {
            super(totalBroadcasts, 0);
        }

        @Override
        public void count(JSONObject message) {
            currentAmountOfBroadcasts++;

            if (currentAmountOfBroadcasts == totalAmountOfBroadcasts)
                isPassed = true;
            if (currentAmountOfBroadcasts > totalAmountOfBroadcasts)
                isPassed = false;
        }
    }

    private final Context context = new Context();
    private Room room = null;
    private static List<Pair<UserProfile, WebSocketConnection>> users = new LinkedList<>();

    private static final int AMOUNT_OF_JOINED_BROADCASTS = 16;  // 1 user — tell him he is accepted. 2 users — send data to #1 about #2 and #2 about #1 plus tell #2 he is joined. 3 users: #1↔#3 and #2↔#3 plus #3→#3, while #1 and #2 already know about themselves.
    private static BroadcastsCounter joinedBroadcasts = new BroadcastsCounter(AMOUNT_OF_JOINED_BROADCASTS, 4); // So, number of broadcast on new user joined is (prev_user_amnt)*2 + 1. And f(1) + f(2) + f(3) + f(4) = 1 + 3 + 5 + 7 = 16. Or simply new_user_amt^2

    private static final int AMOUNT_OF_LEFT_BROADCASTS = 6;  // 3! transmissions for 4 users
    private static BroadcastsCounter leftBroadcasts = new BroadcastsCounter(AMOUNT_OF_LEFT_BROADCASTS, 3);

    private static final int AMOUNT_OF_USER_READY_BROADCASTS = 16;  // 4^2 transmissions for 4 users
    private static BroadcastsCounter readyBroadcasts = new BroadcastsCounter(AMOUNT_OF_USER_READY_BROADCASTS, 4);

    private static final int AMOUNT_OF_WORLD_TILES_BROADCASTS = 512;  // (tile_counts+bombermen)*4 = (31*4 + 4)*4 for basic world
    private static BroadcastsCounter tileBroadcasts = new SimpleBroadcastsCounter(AMOUNT_OF_WORLD_TILES_BROADCASTS);

    private static final int AMNT_OF_WORLD_CREATED_BROADCASTS = 4;  // (tile_counts+bombermen)*4 = (31*4 + 4)*4 for basic world
    private static BroadcastsCounter worldCreatedBroadcasts = new SimpleBroadcastsCounter(AMNT_OF_WORLD_CREATED_BROADCASTS);


    private static final int TOTAL_CASES_TO_TEST = 5;
    private int casesSuccesfullyTested = 0;

}
