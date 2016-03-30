package main;

import main.config.Context;
import main.database.DataBaseHashMapImpl;
import main.database.DataBaseRealImpl;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

@SuppressWarnings("OverlyBroadThrowsClause")
public class Main {
    public static void main(String[] args) throws Exception {
        fillContext();
        setCustomPort(args);
        try { setDataBaseType(args);} catch (Exception ex) { System.out.println("Could not instantiate database.\nQuitting..."); System.exit(1);}

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
         if (args.length >= 1)
             port = Integer.valueOf(args[0]);
         else {
             port = DEFAULT_PORT;
         }
    }

    private static void setDataBaseType(String[] args) throws Exception {
        final AccountService accountService = (AccountService) CONTEXT.get(AccountService.class);
        if (args.length >= 1)
            switch (args[1]) {
                case "hash":
                    accountService.changeDB(new DataBaseHashMapImpl());
                    break;
                case "debug":
                    accountService.changeDB(new DataBaseRealImpl(DataBaseRealImpl.DBTYPE.DEBUG));
                    break;
                default:
                    accountService.changeDB(new DataBaseRealImpl(DataBaseRealImpl.DBTYPE.PRODUCTION));
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
