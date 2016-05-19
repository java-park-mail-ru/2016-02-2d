package bomberman.service;

import main.websockets.MessageSendable;
import rest.UserProfile;

import java.util.List;

public interface RoomManager extends Runnable {
    Room assignUserToFreeRoom(UserProfile user, MessageSendable socket);
    void removeUserFromRoom(UserProfile user);
    List<Room> getAllRooms();
    Room getCurrentRoom();
}
