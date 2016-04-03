package main;

import main.config.Context;
import main.database.DataBase;
import main.database.DataBaseHashMapImpl;
import main.database.DataBaseRealImpl;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

@SuppressWarnings("OverlyBroadThrowsClause")
public class Main {
    public static void main(String[] args) throws Exception {
        setDataBaseType(args);
        createAccountService();
        setCustomPort(args);


        System.out.format("Starting at %d port\n", port);
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
             System.out.format("No port specified. Launching at default %d port.\n", DEFAULT_PORT);
             port = DEFAULT_PORT;
         }
    }

    private static void setDataBaseType(String[] args) throws Exception {
        System.out.println("Instantiating DataBase...");
        DataBase dataBase = null;
        try {
            if (args.length >= 2)
                switch (args[1]) {
                    case "hash":
                        System.out.println("Launching with HashDB");
                        dataBase = new DataBaseHashMapImpl();
                        break;
                    case "debug":
                        System.out.println("Launching with debug DB");
                        dataBase = new DataBaseRealImpl(DataBaseRealImpl.DBTYPE.DEBUG);
                        break;
                    default:
                        System.out.println("Launching with production DB");
                        dataBase = new DataBaseRealImpl(DataBaseRealImpl.DBTYPE.PRODUCTION);
                }
            else {
                System.out.println("No DB type specified. Launching with production DB.");
                dataBase = new DataBaseRealImpl();
            }
        } catch (Exception ex) {
            System.out.println("Cannot instantiate DataBase. Aborting...");
            throw new Exception(ex);
        }

        try {
            CONTEXT.put(DataBase.class, dataBase);
        } catch (InstantiationException ex) {
            System.out.println("Cannot add DataBase to context. Aborting...");
            throw new Exception(ex);
        }
        System.out.println("OK.");
    }

    private static void createAccountService() throws Exception {
        System.out.println("Instantiating AccountService...");
        try {
            final DataBase dataBase = (DataBase) CONTEXT.get(DataBase.class);
            CONTEXT.put(AccountService.class, new AccountServiceImpl(dataBase));
        } catch (InstantiationException ex) {
            System.out.println("Cannot add AccountService to context. Aborting...");
            throw new Exception(ex);
        }
        System.out.println("OK.");
    }


    private static final Context CONTEXT = new Context();
    private static int port;
    private static final int DEFAULT_PORT = 8080;
}
