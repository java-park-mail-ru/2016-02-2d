package main.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.*;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.sql.*;
import java.util.*;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class DataBaseRealImpl implements DataBase, AutoCloseable {

    public DataBaseRealImpl() throws Exception {
        type = DBTYPE.PRODUCTION;
        createDBAndSetup();
    }

    public DataBaseRealImpl(DBTYPE dbtype) throws Exception {
        type = dbtype;
        createDBAndSetup();
    }

    public void createDBAndSetup() throws Exception {
        try {
            setup();
        } catch (HibernateException ex) {
            try {
                logger.warn("Could not connect to DB. Attempting to create it.");
                connectToDBOrDie();
                setup();
            } catch (HibernateException ex2) {
                logger.fatal("Check if MySQL is running and is compatible with 5.1.38 mysql/j.", ex2);
                throw new Exception(ex2);
            }
        }
    }

    public String getLocalStatus() {
        final String status;
        try (Session session = sessionFactory.openSession()) {
            final Transaction transaction = session.beginTransaction();
            status = transaction.getStatus().toString();
            transaction.commit();
        }
        return status;
    }

    @Override
    public void save(UserProfileData dataSet) {
        try (Session session = sessionFactory.openSession()) {
            final Transaction transaction = session.beginTransaction();
            final UserProfileDataDAO dao = new UserProfileDataDAO(session);
            dao.save(dataSet);
            transaction.commit();
        }
    }

    @Override
    @Nullable
    public UserProfile getById(long id) {
        try (Session session = sessionFactory.openSession()) {
            final UserProfileDataDAO dao = new UserProfileDataDAO(session);
            final UserProfileData data = dao.read(id);
            if (data == null)
                return null;
            else
                return new UserProfile(dao.read(id));
        } catch (HibernateException ex) {
            final String reason = "Could not get user #" + id + " by ID!";
            logger.info(reason, ex);
            return null;
        }
    }

    @Override
    @Nullable
    public UserProfile getByLogin(String name) {
        try (Session session = sessionFactory.openSession()) {
            final UserProfileDataDAO dao = new UserProfileDataDAO(session);
            final UserProfileData data = dao.readByName(name);
            if (data == null)
                return null;
            else
                return new UserProfile(dao.readByName(name));
        } catch (HibernateException ex) {
            final String reason = "Could not get user \"" + name + "\" by login!";
            logger.info(reason, ex);
            return null;
        }
    }

    @Override
    @Nullable
    public Collection<UserProfile> getUsers() {
        try (Session session = sessionFactory.openSession()) {
            final UserProfileDataDAO dao = new UserProfileDataDAO(session);

            final LinkedList<UserProfile> result = new LinkedList<>();
            dao.readAll().stream().forEach((data) -> result.add(new UserProfile(data)));    // TODO: investigate how to reduce instantiations!
            return result;
        } catch (HibernateException ex) {
            final String reason = "Could not get all users somewhy! O_o";
            logger.error(reason, ex);
            return null;
        }

    }

    @Override
    @Nullable
    public UserProfile addUser(String login, String password) {
        if (containsLogin(login))
            return null;
        final UserProfileData newUser = new UserProfileData(login, password);
        save(newUser);
        return getByLogin(login);
    }

    @Override
    public boolean containsID(Long id) {
        return getById(id) != null;
    }

    @Override
    public boolean containsLogin(String name) {
        return getByLogin(name) != null;
    }

    @Override
    public void deleteUser(Long id) {
        try (Session session = sessionFactory.openSession()) {
            final UserProfileDataDAO dao = new UserProfileDataDAO(session);
            dao.delete(id);
        } catch (HibernateException ex) {
            final String reason = "Could not delete user #" + id + '!';
            logger.error(reason, ex);
        }
    }

    @Override
    public void close() {
        sessionFactory.close();
    }

    
    private static SessionFactory createSessionFactory(Configuration configuration) {
        final StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
        builder.applySettings(configuration.getProperties());
        final ServiceRegistry serviceRegistry = builder.build();
        return configuration.buildSessionFactory(serviceRegistry);
    }
    
    private void setup() throws HibernateException {
        logger.info("-----Initializing Hibernate-----");
        config = new Config(type);
        final Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(UserProfileData.class);

        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        configuration.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
        configuration.setProperty("hibernate.connection.url", Config.ADDRESS + config.getDbName());
        configuration.setProperty("hibernate.connection.username", config.getLogin());
        configuration.setProperty("hibernate.connection.password", config.getPassword());
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.hbm2ddl.auto", config.getCreationMethod());

        sessionFactory = createSessionFactory(configuration);
        logger.info("-----Hibernate  Initialized-----");
    }

    @SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed"})
    private void connectToDBOrDie() throws Exception {
        Connection rootConnection = null;
        //noinspection OverlyBroadCatchBlock
        try {
            final Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
            DriverManager.registerDriver(driver);
            DriverManager.setLoginTimeout(1);
            rootConnection = DriverManager.getConnection(Config.ADDRESS, Config.ROOT_LOGIN, Config.ROOT_PASSWORD);
            final Statement statement = rootConnection.createStatement();

            statement.execute("DROP USER IF EXISTS " + config.getLoginDomain());
            statement.execute("CREATE USER " + config.getLoginDomain() + " IDENTIFIED BY \"" + config.getPassword() + "\";");

            statement.execute("DROP DATABASE IF EXISTS " + config.getDbName() + ';');
            statement.execute("CREATE DATABASE IF NOT EXISTS " + config.getDbName() + ';');

            statement.execute("GRANT ALL ON " + config.getDbName() + ".* TO " + config.getLoginDomain() + ';');

            logger.info("Database succesfully created!");
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (rootConnection != null) try {rootConnection.close();} catch (SQLException ignore) {/*ignore.printStackTrace();*/}
        }
    }

    private SessionFactory sessionFactory;
    private static final int ERROR_NO_DB = 1049;
    private final DBTYPE type;
    private Config config;

    private static Logger logger = LogManager.getLogger(DataBaseRealImpl.class);

    public enum DBTYPE {
        PRODUCTION, DEBUG
    }

    private static final class Config {

        private Config(DBTYPE dbtype) {
            switch (dbtype) {

                case PRODUCTION:
                    makeProduction();
                    break;
                case DEBUG:
                    makeDebug();
                    break;
            }
        }

        private void makeProduction() {
            dbName = DBNAME;
            login = LOGIN;
            password = PASSWORD;
            loginDomain = LOGIN_DOMAIN;
            creationMethod = DB_UPDATE;
        }

        private void makeDebug() {
            dbName = DBNAME + DEBUG_POSTFIX;
            login = LOGIN + DEBUG_POSTFIX;
            password = PASSWORD + DEBUG_POSTFIX;
            loginDomain = LOGIN + DEBUG_POSTFIX + '@' + DOMAIN;
            creationMethod = DB_CREATEDROP;
        }

        public String getLoginDomain() {
            return loginDomain;
        }

        public String getDbName() {
            return dbName;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public String getCreationMethod() {
            return creationMethod;
        }

        private String dbName;
        private String login;
        private String password;
        private String loginDomain;
        private String creationMethod;

        public static final String ROOT_LOGIN = "root";
        public static final String ROOT_PASSWORD = "root";
        public static final String DBNAME = "bomberman_db";
        public static final String LOGIN = "bomberman_db_user";
        public static final String PASSWORD = "bomberman_db_password";
        public static final String DOMAIN = "localhost";
        public static final String LOGIN_DOMAIN = LOGIN + '@' + DOMAIN;
        public static final String PORT = "3306";
        public static final String ADDRESS = "jdbc:mysql://" + DOMAIN + ':' + PORT + '/';
        public static final String DEBUG_POSTFIX = "_debug";
        public static final String DB_UPDATE = "update";
        public static final String DB_CREATEDROP = "create-drop";
    }

}
