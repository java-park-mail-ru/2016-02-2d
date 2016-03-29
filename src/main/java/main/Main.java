package main;

import main.config.Context;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

@SuppressWarnings("OverlyBroadThrowsClause")
public class Main {
    public static void main(String[] args) throws Exception {
        fillContext();
        setCustomPort(args);

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
        return CONTEXT;
    }

     private static void setCustomPort(String[] args) {
         if (args.length == 1)
             port = Integer.valueOf(args[0]);
         else {
             port = DEFAULT_PORT;
         }
    }

    private static void fillContext() throws Exception {
        System.out.format("-----Initializing context-----\n");
        try {
            CONTEXT.put(AccountService.class, new AccountServiceImpl());
        } catch (InstantiationException ex) {
            System.out.println("Cannot add AccountService to context. Aborting...");
            throw new Exception(ex);
        }
    }


    private static final Context CONTEXT = new Context();
    private static int port;
    private static final int DEFAULT_PORT = 8080;
}
