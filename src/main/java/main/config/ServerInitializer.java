package main.config;

import bomberman.service.RoomManager;
import bomberman.service.RoomManagerImpl;
import main.accountservice.AccountService;
import main.accountservice.AccountServiceImpl;
import main.databaseservice.DataBaseService;
import main.databaseservice.DataBaseServiceHashMapImpl;
import main.databaseservice.DataBaseServiceMySQLImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;


import java.util.Map;

public class ServerInitializer {
    public ServerInitializer(@Nullable String propertyFile) {
        if (propertyFile != null)
            reader = new PropertyReader(propertyFile);
        else
            reader = new PropertyReader();
    }

    public ServerInitializer() {
        reader = new PropertyReader();
    }

    public Map<String, String> getPropertiesMap() {
        return reader.getPropertyMap();
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    public Context fillNewContext() throws Exception {
        final Context context = new Context();

        context.put(DataBaseService.class, createNewDBS());
        context.put(AccountService.class, createNewAccountService((DataBaseService) context.get(DataBaseService.class)));
        context.put(RoomManager.class, createNewRoomManager());

        return context;
    }

    private DataBaseService createNewDBS() throws Exception {
        LOGGER.info("Instantiating DataBaseService...");
        final DataBaseService dataBaseService;
        try {
            if (reader.getPropertyMap().containsKey("db_type"))
                switch (reader.getPropertyMap().get("db_type")) {
                    case "hash":
                        LOGGER.info("Launching with HashDB");
                        dataBaseService = new DataBaseServiceHashMapImpl();
                        break;
                    case "debug":
                        LOGGER.info("Launching with debug DB");
                        dataBaseService = new DataBaseServiceMySQLImpl(reader.getPropertyMap(), DataBaseServiceMySQLImpl.DBTYPE.DEBUG);
                        break;
                    case "production":
                        LOGGER.info("Launching with production DB");
                        dataBaseService = new DataBaseServiceMySQLImpl(reader.getPropertyMap(), DataBaseServiceMySQLImpl.DBTYPE.PRODUCTION);
                        break;
                    default:
                        LOGGER.fatal("Unknown dbtype argument!");
                        throw new Exception();
                }
            else {
                LOGGER.info("No DB type specified. Launching with production DB.");
                dataBaseService = new DataBaseServiceMySQLImpl(reader.getPropertyMap());
            }
        } catch (Exception ex) {
            LOGGER.fatal("Cannot instantiate DataBaseService.");
            throw ex;
        }
        LOGGER.info("OK.");

        return dataBaseService;
    }

    private static AccountService createNewAccountService(DataBaseService dataBaseService) {
        LOGGER.info("Instantiating AccountService...");
        final AccountService accountService = new AccountServiceImpl(dataBaseService);
        LOGGER.info("OK.");
        return accountService;
    }

    private static RoomManager createNewRoomManager() {
        LOGGER.info("Instantiating RoomManager...");
        final RoomManager roomManager = new RoomManagerImpl();
        LOGGER.info("OK.");
        return roomManager;
    }

    private final PropertyReader reader;

    private static final Logger LOGGER = LogManager.getLogger(ServerInitializer.class);
}
