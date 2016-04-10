package main.websocketconnection;


import bomberman.service.RoomManager;
import bomberman.service.RoomManagerImpl;
import main.UserTokenManager;
import main.accountservice.AccountService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.HttpCookie;

public class WebSocketConnectionCreator implements WebSocketCreator {

    public WebSocketConnectionCreator(RoomManager roomManager, AccountService accountService) {
        globalRoomManager = roomManager;
        globalAccountService = accountService;
    }

    @Override
    @Nullable
    public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
        String token = null;
        for (HttpCookie cookie : servletUpgradeRequest.getCookies())
            if (cookie.getName().equals(UserTokenManager.COOKIE_NAME))
                token = cookie.getValue();

        UserProfile user = null;

        try {
            if (token == null) {
                servletUpgradeResponse.sendError(Response.Status.BAD_REQUEST.getStatusCode(), "No cookies specified!");
                logger.debug("No cookies found while upgrading to websocket.");
                return null;
            }
            if (!globalAccountService.hasSessionID(token) || (user = globalAccountService.getBySessionID(token)) == null) {
                servletUpgradeResponse.sendError(Response.Status.UNAUTHORIZED.getStatusCode(), "No suitable user found for this cookie!");
                logger.debug("No suitable user found while upgrading to websocket.");
                return null;
            }
        } catch (IOException ex) {
            logger.info("Aurhorization error while upgrading to websocket. Unable to send refusal.", ex);
        }

        /* TODO: Tell me where is an error:
        //      1: if token == null then return;
        //      2: if no_such_user then return;
        //      3: if user == null then return;
        //      else do_smth_with_user();   // ← user is not null. if it is null goto 3; */
        return new WebSocketConnection(globalRoomManager, user);
    }

    private RoomManager globalRoomManager;
    private AccountService globalAccountService;
    private static Logger logger = LogManager.getLogger(WebSocketConnectionCreator.class);
}
