package bomberman.service;

import bomberman.mechanics.Bomberman;
import bomberman.mechanics.World;
import constants.Constants;
import junit.framework.Assert;
import main.websockets.MessageSendable;
import main.websockets.WebSocketConnection;
import org.javatuples.Pair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import rest.UserProfile;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;

public class RoomFuncTest {

    private void createPlayers() {
        users = new HashSet<>();

        for (int i = 0; i < 4; ++i)
            users.add(Constants.customMockUserProfile("user" + i, "pass" + i, "sid" + i, i));
    }

    @Before
    public void setupRoom() {
        createPlayers();

        room = new Room();
        room.createNewWorld("spiral-world");

        users.forEach((user) -> room.insertPlayer(user, Constants.GameMechanicsMocks.createMessageSendable(user, room)));

    }

    @Test
    public void testWorldActivationOnUsersReady() {
        users.forEach((user) -> room.updatePlayerState(user, true, false));

        assertEquals(false, room.isActive());

        users.forEach((user) -> room.updatePlayerState(user, false, true));

        assertEquals(false, room.isActive());

        users.forEach((user) -> room.updatePlayerState(user, true, true));

        assertEquals(false, room.isActive());

        TimeHelper.sleepFor(Room.TIME_TO_WAIT_AFTER_READY + ALLOWED_TIME_ERROR);

        assertEquals(true, room.isActive());
    }

    @Test
    public void testGameOverAndDraw() {
        users.forEach((user) -> room.updatePlayerState(user, true, true));
        TimeHelper.sleepFor(Room.TIME_TO_WAIT_AFTER_READY + ALLOWED_TIME_ERROR);

        users.forEach((user) -> room.scheduleBombPlacement(user));

        TimeHelper.sleepFor(Room.TIME_TO_WAIT_ON_GAME_OVER * 10 + ALLOWED_TIME_ERROR + (long) (Bomberman.BOMB_BASE_EXPLOSION_DELAY * 1000));    // TODO: Rewrite update() to run in a separate thread!!!
        assertEquals(0, room.getWorld().getBombermenIDs().length);
    }

    private Set<UserProfile> users;
    private Room room;

    private final static int ALLOWED_TIME_ERROR = 10; // ms
}
