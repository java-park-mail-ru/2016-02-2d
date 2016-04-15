package main.websocketconnection;

import bomberman.service.RoomManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import rest.UserProfile;

import java.io.IOException;

@WebSocket
public class WebSocketConnection {
    public WebSocketConnection(RoomManager roomManager, UserProfile owner) {
        user = owner;
        globalRoomManager = roomManager;
    }

    @OnWebSocketMessage
    public void onMessage(String data) {
        try {
            session.getRemote().sendString("Hi!");
        }
        catch (IOException ex) {
            LOGGER.error("Could not send message to user #" + user.getId() + " (\"" + user.getLogin() + "\")!", ex);
        }
    }

    @OnWebSocketConnect
    public void onOpen(Session sess) {
        session = sess;
        globalRoomManager.assignUserToFreeRoom(user, session);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        globalRoomManager.removeUserFromRoom(user);
    }

    private final UserProfile user;
    private final RoomManager globalRoomManager;
    private Session session;
    private static final Logger LOGGER = LogManager.getLogger(WebSocketConnection.class);
}
