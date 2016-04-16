package bomberman.service;

import bomberman.mechanics.interfaces.WorldType;
import main.websocketconnection.MessageSendable;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class RoomManagerImpl implements RoomManager {

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

    private Room createNewRoom(String worldType) {
        final Room room = new Room();

        room.createNewWorld(WorldType.BASIC_WORLD);
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

    private final PriorityQueue<Room> nonFilledRooms = new PriorityQueue<>();
    private final ArrayList<Room> allRooms = new ArrayList<>();
    private final Map<UserProfile, Room> playerWhereabouts = new HashMap<>();
}
