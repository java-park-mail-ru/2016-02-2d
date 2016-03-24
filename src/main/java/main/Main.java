package main;

import main.config.Context;
import main.config.Port;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

@SuppressWarnings("DuplicateThrows")
public class Main {
    public static void main(String[] args) throws Exception, InterruptedException {
        fillContext();
        setCustomPort(args);

        final int port = ((Port)context.get(Port.class)).getPort();
        System.out.format("Starting at port: %d\n", port);
        final Server server = new Server(port);
        final ServletContextHandler contextHandler = new ServletContextHandler(server, "/api/", ServletContextHandler.SESSIONS);

        final ServletHolder servletHolder = new ServletHolder(ServletContainer.class);
        servletHolder.setInitParameter("javax.ws.rs.Application","main.RestApplication");

        contextHandler.addServlet(servletHolder, "/*");
        server.start();
        server.join();
    }

    public static Context getContext() {
        return context;
    }

     private static void setCustomPort(String[] args) {
        final Port port;
         if (args.length == 1)
            port = new Port(Integer.valueOf(args[0]));
         else {
            System.err.format("Port is not specified, setting it to %d\n", DEFAULT_PORT);
            port = new Port(DEFAULT_PORT);
         }
         context.put(Port.class, port);
    }

    private static void fillContext() {
        System.out.format("Initializing context...\n");
        context.put(AccountService.class, new AccountService());
    }


    private static Context context = new Context();
    private static final int DEFAULT_PORT = 8080;
}
