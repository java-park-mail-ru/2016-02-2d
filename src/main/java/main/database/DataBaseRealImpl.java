package main.database;

import org.hibernate.*;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.sql.*;
import java.util.*;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class DataBaseRealImpl implements DataBase {

    public DataBaseRealImpl() {
        type = DBTYPE.PRODUCTION;
        createDBAndSetup();
    }

    public DataBaseRealImpl(DBTYPE dbtype) {
        type = dbtype;
        createDBAndSetup();
    }

    public void createDBAndSetup() {
        try {
            setup();
        } catch (HibernateException ex) {
            createDB();
            setup();
        }
    }

    public String getLocalStatus() {
        String status;
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
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
            System.out.format("Could not get user #%d by ID!\nMessage: %s\nCause: %s", id, ex.getMessage(), ex.getCause());
            ex.printStackTrace();
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
            System.out.format("Could not get user \"%s\" by login!\nMessage: %s\nCause: %s", name, ex.getMessage(), ex.getCause());
            ex.printStackTrace();
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
            System.out.format("Could not get all users somewhy! O_o\nMessage: %s\nCause: %s", ex.getMessage(), ex.getCause());
            ex.printStackTrace();
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
            System.out.format("Could not delete user #%d!\nMessage: %s\nCause: %s", id, ex.getMessage(), ex.getCause());
            ex.printStackTrace();
        }
    }

    // TODO: figure out how to use this at the very end. Autoclosables?
    public void shutdown() {
        sessionFactory.close();
    }

    private static SessionFactory createSessionFactory(Configuration configuration) {
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
        builder.applySettings(configuration.getProperties());
        ServiceRegistry serviceRegistry = builder.build();
        return configuration.buildSessionFactory(serviceRegistry);
    }

    private void setup() throws HibernateException {
        System.out.println("-----Initializing Hibernate-----");
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(UserProfileData.class);

        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        configuration.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
        configuration.setProperty("hibernate.connection.url", ADDRESS + DBNAME);
        configuration.setProperty("hibernate.connection.username", LOGIN);
        configuration.setProperty("hibernate.connection.password", PASSWORD);
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");

        sessionFactory = createSessionFactory(configuration);
        System.out.println("-----Hibernate  Initialized-----");
    }

    private void createDB() {
        try {
            Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
            final Connection rootConnection = DriverManager.getConnection(ADDRESS, ROOT_LOGIN, ROOT_PASSWORD);
            Statement statement = rootConnection.createStatement();

            statement.execute("DROP USER IF EXISTS " + LOGIN_DOMAIN);
            statement.execute("CREATE USER " + LOGIN_DOMAIN + " IDENTIFIED BY \"" + PASSWORD + "\";");

            statement.execute("DROP DATABASE IF EXISTS " + DBNAME + ';');
            statement.execute("CREATE DATABASE IF NOT EXISTS " + DBNAME + ';');

            statement.execute("GRANT ALL ON " + DBNAME + ".* TO " + LOGIN_DOMAIN + ';');

            System.out.println("Database succesfully created!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private SessionFactory sessionFactory;
    private static final int ERROR_NO_DB = 1049;
    private DBTYPE type;

    public enum DBTYPE {
        PRODUCTION, DEBUG
    }

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


}
