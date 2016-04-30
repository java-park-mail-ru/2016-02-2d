package bomberman.service;

import bomberman.mechanics.WorldBuilderForeman;
import main.websockets.MessageSendable;
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

    private final Queue<Room> nonFilledRooms = new LinkedList<>();
    private final ArrayList<Room> allRooms = new ArrayList<>();
    private final Map<UserProfile, Room> playerWhereabouts = new HashMap<>();
}
