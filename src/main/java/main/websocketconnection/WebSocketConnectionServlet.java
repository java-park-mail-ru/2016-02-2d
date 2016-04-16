package main.websocketconnection;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "WebSocketConnectionServlet", urlPatterns = {"/game"})
public class WebSocketConnectionServlet extends WebSocketServlet {

    public WebSocketConnectionServlet(int timeout) {
        idleTime = timeout;
    }

    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.getPolicy().setIdleTimeout(idleTime);
        webSocketServletFactory.setCreator(new WebSocketConnectionCreator());
    }

    private final int idleTime;
}
