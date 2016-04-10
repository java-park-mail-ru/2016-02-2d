package bomberman.service;

import org.eclipse.jetty.websocket.api.Session;
import rest.UserProfile;

public interface RoomManager {
    void assignUserToFreeRoom(UserProfile user, Session session);
    void removeUserFromRoom(UserProfile user);
}
