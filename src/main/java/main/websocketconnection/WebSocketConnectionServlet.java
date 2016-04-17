package main.websocketconnection;

import main.config.Context;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "WebSocketConnectionServlet", urlPatterns = {"/game"})
public class WebSocketConnectionServlet extends WebSocketServlet {

    public WebSocketConnectionServlet(Context context,int timeout) {
        idleTime = timeout;
        this.context = context;
    }

    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.getPolicy().setIdleTimeout(idleTime);
        webSocketServletFactory.setCreator(new WebSocketConnectionCreator(context));
    }

    private final int idleTime;
    private final Context context;
}
