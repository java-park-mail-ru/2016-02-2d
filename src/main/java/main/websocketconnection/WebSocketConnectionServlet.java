package main.websocketconnection;

import bomberman.service.RoomManager;
import bomberman.service.RoomManagerImpl;
import main.accountservice.AccountService;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "WebSocketConnectionServlet", urlPatterns = {"/game"})
public class WebSocketConnectionServlet extends WebSocketServlet {

    public WebSocketConnectionServlet(RoomManager roomManager, AccountService accountService) {
        globalRoomManager = roomManager;
        globalAccountService = accountService;
    }

    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.getPolicy().setIdleTimeout(IDLE_TIME);
        webSocketServletFactory.setCreator(new WebSocketConnectionCreator(globalRoomManager, globalAccountService));
    }

    private static final int IDLE_TIME = 60 * 1000; // TODO: move to config
    private RoomManager globalRoomManager;
    private AccountService globalAccountService;
}
