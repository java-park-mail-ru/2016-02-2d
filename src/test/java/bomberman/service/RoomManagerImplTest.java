package bomberman.service;

import constants.Constants;
import main.websocketconnection.MessageSendable;
import org.javatuples.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import rest.UserProfile;

import java.util.ArrayList;

import static org.junit.Assert.*;


public class RoomManagerImplTest {

    @BeforeClass
    public static void init() {
        mockUsers.add(new Pair<>(Constants.customMockUserProfile("u1", "p1", "sid1", 1), Constants.uniqueMockMessageSendable()));
        mockUsers.add(new Pair<>(Constants.customMockUserProfile("u2", "p2", "sid2", 2), Constants.uniqueMockMessageSendable()));
        mockUsers.add(new Pair<>(Constants.customMockUserProfile("u3", "p3", "sid3", 3), Constants.uniqueMockMessageSendable()));
        mockUsers.add(new Pair<>(Constants.customMockUserProfile("u4", "p4", "sid4", 4), Constants.uniqueMockMessageSendable()));
        mockUsers.add(new Pair<>(Constants.customMockUserProfile("u5", "p5", "sid5", 5), Constants.uniqueMockMessageSendable()));
    }

    @Test
    public void testAssignUserToFreeRoom() throws Exception {
        final RoomManager roomManager = new RoomManagerImpl();
        final Room room1 = roomManager.assignUserToFreeRoom(mockUsers.get(0).getValue0(), mockUsers.get(0).getValue1());

        assertEquals(room1, roomManager.assignUserToFreeRoom(mockUsers.get(1).getValue0(), mockUsers.get(1).getValue1()));
        assertEquals(room1, roomManager.assignUserToFreeRoom(mockUsers.get(2).getValue0(), mockUsers.get(2).getValue1()));
        assertEquals(room1, roomManager.assignUserToFreeRoom(mockUsers.get(3).getValue0(), mockUsers.get(3).getValue1()));
        assertNotEquals(room1, roomManager.assignUserToFreeRoom(mockUsers.get(4).getValue0(), mockUsers.get(4).getValue1()));
    }

    @Test
    public void testRemoveUserFromRoom() throws Exception {
        final RoomManager roomManager = new RoomManagerImpl();
        final Room room1 = roomManager.assignUserToFreeRoom(mockUsers.get(0).getValue0(), mockUsers.get(0).getValue1());
        assertEquals(true, room1.hasPlayer(mockUsers.get(0).getValue0()));
        roomManager.removeUserFromRoom(mockUsers.get(0).getValue0());
        assertEquals(false, room1.hasPlayer(mockUsers.get(0).getValue0()));
        assertNotEquals(room1, roomManager.assignUserToFreeRoom(mockUsers.get(0).getValue0(), mockUsers.get(0).getValue1()));
    }

    static final ArrayList<Pair<UserProfile, MessageSendable>> mockUsers = new ArrayList<>();
}