package bomberman.service;

import bomberman.mechanics.WorldBuilderForeman;
import main.websockets.MessageSendable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoomManagerImpl implements RoomManager {

    public RoomManagerImpl() {
        createNewRoom("any");
    }

    @Override
    public Room assignUserToFreeRoom(UserProfile user, MessageSendable socket) {
        removeUserFromRoom(user);

        final Room room = getNonFilledNotActiveRoom();

        room.insertPlayer(user, socket);
        playerWhereabouts.put(user, room);
        if (room.isFilled())
            nonFilledRooms.remove();

        return room;
    }

    @Override
    public void removeUserFromRoom(UserProfile user) {
        final Room room = getRoomByUser(user);

        if (room != null) {
            playerWhereabouts.remove(user);
            room.removePlayer(user);

            if (room.isEmpty()) {
                allRooms.remove(room);
                nonFilledRooms.remove(room);
            } else
                if (!room.isActive() && !nonFilledRooms.contains(room))
                    nonFilledRooms.add(room);
        }
    }

    @Override
    public List<Room> getAllRooms() {
        return allRooms;
    }

    private Room getNonFilledNotActiveRoom() {
        while (true) {
            final Room room = nonFilledRooms.peek();
            if (room == null)
                return createNewRoom("");
            else
                if (room.isActive())
                    nonFilledRooms.remove();
                else
                    return room;
        }
    }

    private Room createNewRoom(String worldType) {
        final Room room = new Room();

        room.createNewWorld(WorldBuilderForeman.getRandomWorldName());
        nonFilledRooms.add(room);
        allRooms.add(room);

        return room;
    }

    @Nullable
    private Room getRoomByUser(UserProfile user) {
        if (playerWhereabouts.containsKey(user))
            return playerWhereabouts.get(user);
        else
            return null;
    }

    @Override
    public void run() {
        long previousTickDuration = Room.MINIMAL_TIME_STEP;

        while (!shouldBeInterrupted){
            final long beforeUpdate = TimeHelper.now();
            boolean wereAnyRoomUpdated = false;

            for (Room room: allRooms) {
                final boolean wasRoomUpdated = room.updateIfNeeded(previousTickDuration);
                if (!wereAnyRoomUpdated && wasRoomUpdated)
                    wereAnyRoomUpdated = true;
            }

            final long totalUpdateTook = TimeHelper.now() - beforeUpdate;

            TimeHelper.sleepFor(Room.MINIMAL_TIME_STEP - totalUpdateTook);

            previousTickDuration = TimeHelper.now() - beforeUpdate;
            logGameCycleTime(totalUpdateTook);

            if (!wereAnyRoomUpdated)
                TimeHelper.sleepFor(100);
        }
    }

    public void interruptNow() {
        shouldBeInterrupted = true;
    }

    private void logGameCycleTime(long timeSpentWhileRunning) {
        if (timeSpentWhileRunning >= Room.MINIMAL_TIME_STEP)
            LOGGER.warn("RoomManager " + this.toString() + " updated. It took " + timeSpentWhileRunning + " >= " + Room.MINIMAL_TIME_STEP + "! Fix the bugs!");
        else
            LOGGER.debug("RoomManager " + this.toString() + " updated. It took " + timeSpentWhileRunning + " < " + Room.MINIMAL_TIME_STEP + ". OK.");
    }

    private final Queue<Room> nonFilledRooms = new ConcurrentLinkedQueue<>();
    private final CopyOnWriteArrayList<Room> allRooms = new CopyOnWriteArrayList<>();
    private final Map<UserProfile, Room> playerWhereabouts = new ConcurrentHashMap<>();

    private boolean shouldBeInterrupted = false;

    private static final Logger LOGGER = LogManager.getLogger(RoomManagerImpl.class);
}
