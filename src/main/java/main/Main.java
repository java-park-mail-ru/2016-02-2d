package main;

import main.config.Context;
import main.database.DataBase;
import main.database.DataBaseHashMapImpl;
import main.database.DataBaseRealImpl;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("OverlyBroadThrowsClause")
public class Main {
    public static void main(String[] args) throws Exception {
        setDataBaseType(args);
        createAccountService();
        setCustomPort(args);

        logger.info("Starting at " + port + " port");
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
             logger.info("No port specified. Launching at default " + DEFAULT_PORT + " port.");
             port = DEFAULT_PORT;
         }
    }

    private static void setDataBaseType(String[] args) throws Exception {
        logger.info("Instantiating DataBase...");
        DataBase dataBase = null;
        try {
            if (args.length >= 2)
                switch (args[1]) {
                    case "hash":
                        logger.info("Launching with HashDB");
                        dataBase = new DataBaseHashMapImpl();
                        break;
                    case "debug":
                        logger.info("Launching with debug DB");
                        dataBase = new DataBaseRealImpl(DataBaseRealImpl.DBTYPE.DEBUG);
                        break;
                    default:
                        logger.info("Launching with production DB");
                        dataBase = new DataBaseRealImpl(DataBaseRealImpl.DBTYPE.PRODUCTION);
                }
            else {
                logger.info("No DB type specified. Launching with production DB.");
                dataBase = new DataBaseRealImpl();
            }
        } catch (Exception ex) {
            logger.fatal("Cannot instantiate DataBase. Aborting...");
            throw new Exception(ex);
        }

        try {
            CONTEXT.put(DataBase.class, dataBase);
        } catch (InstantiationException ex) {
            logger.fatal("Cannot add DataBase to context. Aborting...");
            throw new Exception(ex);
        }
        logger.info("OK.");
    }

    private static void createAccountService() throws Exception {
        logger.info("Instantiating AccountService...");
        try {
            final DataBase dataBase = (DataBase) CONTEXT.get(DataBase.class);
            CONTEXT.put(AccountService.class, new AccountServiceImpl(dataBase));
        } catch (InstantiationException ex) {
            logger.fatal("Cannot add AccountService to context. Aborting...");
            throw new Exception(ex);
        }
        logger.info("OK.");
    }


    private static final Context CONTEXT = new Context();
    private static int port;
    private static final int DEFAULT_PORT = 8080;

    private static Logger logger = LogManager.getLogger(Main.class);
}
