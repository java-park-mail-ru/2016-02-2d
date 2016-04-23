package bomberman.service;

import main.websocketconnection.MessageSendable;
import rest.UserProfile;

import java.util.List;
import java.util.Set;

public interface RoomManager {
    Room assignUserToFreeRoom(UserProfile user, MessageSendable socket);
    void removeUserFromRoom(UserProfile user);
    List<Room> getAllRooms();
}
