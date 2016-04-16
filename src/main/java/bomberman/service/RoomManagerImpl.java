package bomberman.service;

import bomberman.mechanics.interfaces.WorldType;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class RoomManagerImpl implements RoomManager {

    @Override
    public Room assignUserToFreeRoom(UserProfile user, Session session) {
        Room room = nonFilledRooms.peek();
        if (room == null)
            room = createNewRoom();
        room.insertPlayer(user, session);
        if (room.isFilled())
            nonFilledRooms.remove();
        return room;
    }

    @Override
    public void removeUserFromRoom(UserProfile user) {
        final Room room = getRoomByUser(user);
        if (room != null) {
            room.removePlayer(user);
            if (room.isEmpty()) {
                allRooms.remove(room);
                nonFilledRooms.remove(room);
            } else
                nonFilledRooms.add(room);
        }
    }

    private Room createNewRoom() {
        final Room room = new Room();
        room.createNewWorld(WorldType.BASIC_WORLD);
        nonFilledRooms.add(room);
        allRooms.add(room);
        return room;
    }

    @Nullable
    private Room getRoomByUser(UserProfile user) {
        for (Room room : allRooms)
            if (room.hasPlayer(user))
                return room;
        return null;
    }

    private final PriorityQueue<Room> nonFilledRooms = new PriorityQueue<>();
    private final ArrayList<Room> allRooms = new ArrayList<>();
}
