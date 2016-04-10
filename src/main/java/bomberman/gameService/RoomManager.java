package bomberman.gameService;

import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class RoomManager {

    public void assignUserToFreeRoom(UserProfile user) {
        Room room = nonFilledRooms.peek();
        if (room == null)
            room = createNewRoom();
        room.insertPlayer(user);
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

    private PriorityQueue<Room> nonFilledRooms;
    private ArrayList<Room> allRooms;
}
