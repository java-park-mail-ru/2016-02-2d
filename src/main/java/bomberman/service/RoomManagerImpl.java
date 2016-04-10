package bomberman.service;

import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class RoomManagerImpl implements RoomManager {

    public void assignUserToFreeRoom(UserProfile user, Session session) {
        Room room = nonFilledRooms.peek();
        if (room == null)
            room = createNewRoom();
        room.insertPlayer(user, session);
        if (room.isFilled())
            nonFilledRooms.remove();
    }

    public void removeUserFromRoom(UserProfile user) {
        Room room = getRoomByUser(user);
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
        Room room = new Room();
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

    private PriorityQueue<Room> nonFilledRooms = new PriorityQueue<>();
    private ArrayList<Room> allRooms = new ArrayList<>();
}
