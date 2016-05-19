package bomberman.service;

import main.websockets.MessageSendable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.UserProfile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RoomManagerConcurImpl implements RoomManager {

    public RoomManagerConcurImpl(int numOfManagers) {
        roomManagers = new RoomManager[numOfManagers];

        for (int i = 0; i < numOfManagers; ++i)
            roomManagers[i] = new RoomManagerImpl();
    }

    @Override
    public Room assignUserToFreeRoom(UserProfile user, MessageSendable socket) {
        final RoomManager manager = getAppropriateRoomManager();

        playerWhereabouts.put(user, manager);
        return manager.assignUserToFreeRoom(user, socket);
    }

    @Override
    public void removeUserFromRoom(UserProfile user) {
        final RoomManager manager = playerWhereabouts.get(user);
        playerWhereabouts.remove(user);

        manager.removeUserFromRoom(user);
    }

    @Override
    public List<Room> getAllRooms() {
        final List<Room> allRooms = new LinkedList<>();

        for (RoomManager manager: roomManagers)
            allRooms.addAll(manager.getAllRooms());

        return allRooms;
    }

    @Override
    public void run() {

        for (RoomManager manager: roomManagers)
            (new Thread(manager)).start();
    }

    private RoomManager getAppropriateRoomManager() {
        if (roomManagers[lastManager].getCurrentRoom().isActive())
            lastManager = lastManager++ % roomManagers.length;
        return roomManagers[lastManager];
    }

    @Override
    public Room getCurrentRoom() {
        return roomManagers[lastManager].getCurrentRoom();
    }

    public static final int DEFAULT_THREADS_AMOUNT = 4;

    private int lastManager = 0;
    private final RoomManager[] roomManagers;
    private final Map<UserProfile, RoomManager> playerWhereabouts = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LogManager.getLogger(RoomManagerConcurImpl.class);
}
