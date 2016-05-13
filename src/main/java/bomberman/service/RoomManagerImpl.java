package bomberman.service;

import bomberman.mechanics.WorldBuilderForeman;
import main.websockets.MessageSendable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.*;

public class RoomManagerImpl implements RoomManager {

    public RoomManagerImpl() {
        createNewRoom("any");
    }

    @Override
    public Room assignUserToFreeRoom(UserProfile user, MessageSendable socket) {
        removeUserFromRoom(user);

        Room room = nonFilledRooms.peek();

        if (room == null)
            room = createNewRoom("");
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
                if (!room.isActive())
                    nonFilledRooms.add(room);
        }
    }

    @Override
    public List<Room> getAllRooms() {
        return allRooms;
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

            for (Room room: allRooms)
                room.updateIfNeeded(previousTickDuration);

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

    private final Queue<Room> nonFilledRooms = new LinkedList<>();
    private final ArrayList<Room> allRooms = new ArrayList<>();
    private final Map<UserProfile, Room> playerWhereabouts = new HashMap<>();

    private boolean shouldBeInterrupted = false;

    private static final Logger LOGGER = LogManager.getLogger(RoomManagerImpl.class);
}
