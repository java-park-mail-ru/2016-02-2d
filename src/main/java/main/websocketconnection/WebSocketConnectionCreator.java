package main.websocketconnection;


import main.UserTokenManager;
import main.accountservice.AccountService;
import main.config.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.HttpCookie;

public class WebSocketConnectionCreator implements WebSocketCreator {
    @Inject
    private Context context;

    public WebSocketConnectionCreator() {
        accountService = (AccountService) context.get(AccountService.class);
    }

    @Override
    @Nullable
    public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
        String token = null;
        for (HttpCookie cookie : servletUpgradeRequest.getCookies())
            if (cookie.getName().equals(UserTokenManager.COOKIE_NAME))
                token = cookie.getValue();

        final UserProfile user;

        try {
            if (token == null) {
                servletUpgradeResponse.sendError(Response.Status.BAD_REQUEST.getStatusCode(), "No cookies specified!");
                LOGGER.debug("No cookies found while upgrading to websocket.");
                return null;
            }
            if (!accountService.hasSessionID(token) || (user = accountService.getBySessionID(token)) == null) {
                servletUpgradeResponse.sendError(Response.Status.UNAUTHORIZED.getStatusCode(), "No suitable user found for this cookie!");
                LOGGER.debug("No suitable user found while upgrading to websocket.");
                return null;
            }
        } catch (IOException ex) {
            LOGGER.info("Authorization error while upgrading to websocket. Unable to send refusal.", ex);
            return null;
        }

        return new WebSocketConnection(user);
    }

    private final AccountService accountService;
    private static final Logger LOGGER = LogManager.getLogger(WebSocketConnectionCreator.class);
}
