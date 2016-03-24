package main;

import main.config.Context;
import main.config.Port;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

@SuppressWarnings("OverlyBroadThrowsClause")
public class Main {
    public static void main(String[] args) throws Exception {
        fillContext();
        setCustomPort(args);

        int port = ((Port) CONTEXT.get(Port.class)).getPort();
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

     private static void setCustomPort(String[] args) throws Exception {
        Port port;
         if (args.length == 1)
            port = new Port(Integer.valueOf(args[0]));
         else {
            System.err.format("Port is not specified, setting it to %d\n", DEFAULT_PORT);
            port = new Port(DEFAULT_PORT);
         }

         try { CONTEXT.put(Port.class, port); }
         catch (InstantiationException ex) {
             System.out.println("Cannot add port value to context. Aborting...");
             throw new Exception(ex);
         }
    }

    private static void fillContext() throws Exception {
        System.out.format("Initializing context...\n");
        try {
            CONTEXT.put(AccountService.class, new AccountService());
        } catch (InstantiationException ex) {
            System.out.println("Cannot add AccountServiceInterface to context. Aborting...");
            throw new Exception(ex);
        }
    }


    private static final Context CONTEXT = new Context();
    private static final int DEFAULT_PORT = 8080;
}
