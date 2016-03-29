package main.database;

import org.hibernate.*;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.*;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class DataBase {

    public DataBase() {
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(UserProfileData.class);

        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        configuration.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
        configuration.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/bomberman_db");
        configuration.setProperty("hibernate.connection.username", "root");
        configuration.setProperty("hibernate.connection.password", "root");
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.hbm2ddl.auto", "create");

        sessionFactory = createSessionFactory(configuration);
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

    public void save(UserProfileData dataSet) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            UserProfileDataDAO dao = new UserProfileDataDAO(session);
            dao.save(dataSet);
            transaction.commit();
        }
    }

    @Nullable
    public UserProfile getById(long id) {
        try (Session session = sessionFactory.openSession()) {
            final UserProfileDataDAO dao = new UserProfileDataDAO(session);
            return new UserProfile(dao.read(id));
        } catch (HibernateException ex) {
            System.out.format("Could not get user #%d by ID!\nMessage: %s\nCause: %s", id, ex.getMessage(), ex.getCause());
            ex.printStackTrace();
            return null;
        }
    }

    @Nullable
    public UserProfile getByLogin(String name) {
        try (Session session = sessionFactory.openSession()) {
            final UserProfileDataDAO dao = new UserProfileDataDAO(session);
            return new UserProfile(dao.readByName(name));
        } catch (HibernateException ex) {
            System.out.format("Could not get user \"%s\" by login!\nMessage: %s\nCause: %s", name, ex.getMessage(), ex.getCause());
            ex.printStackTrace();
            return null;
        }
    }

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

    @Nullable
    public UserProfile addUser(String login, String password) {
        if (containsLogin(login))
            return null;
        final UserProfileData newUser = new UserProfileData(login, password);
        save(newUser);
        return getByLogin(login);
    }


    public boolean containsID(Long id) {
        return getById(id) != null;
    }

    public boolean containsLogin(String name) {
        return getByLogin(name) != null;
    }

    public void deleteUser(Long id) {
        try (Session session = sessionFactory.openSession()) {
            final UserProfileDataDAO dao = new UserProfileDataDAO(session);
            dao.delete(dao.read(id));       // TODO: figure out how to remove record by id without inflicting redundant object creations... StackOverflow claims this to be most effective way.
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

    private SessionFactory sessionFactory;

}
