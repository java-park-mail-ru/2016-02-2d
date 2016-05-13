package bomberman.service;

import bomberman.mechanics.WorldBuilderForeman;
import main.websockets.MessageSendable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManagerConcurImpl implements RoomManager {

    public RoomManagerConcurImpl(int numOfManagers) {
        roomManagers = new RoomManager[numOfManagers];

        for (int i = 0; i < numOfManagers; ++i)
            roomManagers[i] = new RoomManagerImpl();
    }

    @Override
    public Room assignUserToFreeRoom(UserProfile user, MessageSendable socket) {
        final RoomManager manager = getLessFilledManager();

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

    private RoomManager getLessFilledManager() {
        int minimalFillage = Integer.MAX_VALUE;
        int minID = 0;

        for (int i = 0; i < roomManagers.length; ++i)
            if (roomManagers[i].getAllRooms().size() < minimalFillage) {
                minimalFillage = roomManagers[i].getAllRooms().size();
                minID = i;
            }

        return roomManagers[minID];
    }

    public static final int DEFAULT_THREADS_AMOUNT = 4;

    private final RoomManager[] roomManagers;
    private final Map<UserProfile, RoomManager> playerWhereabouts = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LogManager.getLogger(RoomManagerConcurImpl.class);
}
