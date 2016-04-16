package bomberman.service;

import constants.Constants;
import main.websocketconnection.MessageSendable;
import org.javatuples.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import rest.UserProfile;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class RoomTest {

    @BeforeClass
    public static void init() {
        mockUsers.add(new Pair<>(Constants.customMockUserProfile("u1", "p1", "sid1", 1), Constants.uniqueMockMessageSendable()));
        mockUsers.add(new Pair<>(Constants.customMockUserProfile("u2", "p2", "sid2", 2), Constants.uniqueMockMessageSendable()));
        mockUsers.add(new Pair<>(Constants.customMockUserProfile("u3", "p3", "sid3", 3), Constants.uniqueMockMessageSendable()));
        mockUsers.add(new Pair<>(Constants.customMockUserProfile("u4", "p4", "sid4", 4), Constants.uniqueMockMessageSendable()));
    }

    @Test
    public void testGetCurrentCapacity() throws Exception {
        final Room room = new Room();
        assertEquals(0, room.getCurrentCapacity());
        room.insertPlayer(mockUsers.get(0).getValue0(), mockUsers.get(0).getValue1());
        assertEquals(1, room.getCurrentCapacity());
        room.insertPlayer(mockUsers.get(1).getValue0(), mockUsers.get(1).getValue1());
        assertEquals(2, room.getCurrentCapacity());
        room.removePlayer(mockUsers.get(0).getValue0());
        assertEquals(1, room.getCurrentCapacity());
    }

    @Test
    public void testIsFilled() throws Exception {
        final Room room = new Room();
        assertEquals(false, room.isFilled());
        room.insertPlayer(mockUsers.get(0).getValue0(), mockUsers.get(0).getValue1());
        room.insertPlayer(mockUsers.get(1).getValue0(), mockUsers.get(1).getValue1());
        room.insertPlayer(mockUsers.get(2).getValue0(), mockUsers.get(2).getValue1());
        room.insertPlayer(mockUsers.get(3).getValue0(), mockUsers.get(3).getValue1());
        assertEquals(true, room.isFilled());
    }

    @Test
    public void testIsEmpty() throws Exception {
        final Room room = new Room();
        assertEquals(true, room.isEmpty());
        room.insertPlayer(mockUsers.get(0).getValue0(), mockUsers.get(0).getValue1());
        assertEquals(false, room.isEmpty());
    }

    @Test
    public void testInsertPlayer() throws Exception {
        final Room room = new Room();
        assertEquals(false, room.hasPlayer(mockUsers.get(0).getValue0()));
        room.insertPlayer(mockUsers.get(0).getValue0(), mockUsers.get(0).getValue1());
        assertEquals(true, room.hasPlayer(mockUsers.get(0).getValue0()));
    }

    @Test
    public void testHasPlayer() throws Exception {
        final Room room = new Room();
        assertEquals(false, room.hasPlayer(mockUsers.get(0).getValue0()));
        room.insertPlayer(mockUsers.get(0).getValue0(), mockUsers.get(0).getValue1());
        assertEquals(true, room.hasPlayer(mockUsers.get(0).getValue0()));
    }

    @Test
    public void testRemovePlayer() throws Exception {
        final Room room = new Room();
        room.insertPlayer(mockUsers.get(0).getValue0(), mockUsers.get(0).getValue1());
        assertEquals(true, room.hasPlayer(mockUsers.get(0).getValue0()));
        room.removePlayer(mockUsers.get(0).getValue0());
        assertEquals(false, room.hasPlayer(mockUsers.get(0).getValue0()));
    }

    static final ArrayList<Pair<UserProfile, MessageSendable>> mockUsers = new ArrayList<>();
}