package main;

import org.junit.Test;
import rest.UserProfile;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Frog on 16.03.2016.
 */
public class AccountServiceTest {

    @Test
    public void testLoginUser() throws Exception {
        AccountService as = new AccountService();
        UserProfile user = as.getUser("admin");

        assertNotNull(user);

        String sid = user.getSessionID();
        as.loginUser(user);

        assertEquals(user, as.getBySessionID(sid));

    }

    @Test
    public void testGetBySessionID() throws Exception {
        AccountService as = new AccountService();
        UserProfile user = as.getUser("admin");

        assertNotNull(user);

        String sid = user.getSessionID();
        as.loginUser(user);

        assertEquals(user, as.getBySessionID(sid));

        assertNull(as.getBySessionID(""));
    }

    @Test
    public void testHasSessionID() throws Exception {
        AccountService as = new AccountService();
        UserProfile user = as.getUser("admin");

        assertNotNull(user);

        String sid = user.getSessionID();

        assertEquals(false, as.hasSessionID(sid));

        as.loginUser(user);

        assertEquals(true, as.hasSessionID(sid));
    }

    @Test
    public void testLogoutUser() throws Exception {
        AccountService as = new AccountService();
        UserProfile user = as.getUser("admin");

        assertNotNull(user);

        String sid = user.getSessionID();

        assertEquals(false, as.logoutUser(sid));

        as.loginUser(user);

        assertEquals(true, as.logoutUser(sid));
    }

    @Test
    public void testCreateNewUser() throws Exception {
        AccountService as = new AccountService();
        final String login = "login";
        final String password = "password";

        UserProfile newUser = as.createNewUser(login, password);

        assertNotNull(newUser);
        assertEquals(login, newUser.getLogin());
        assertEquals(password, newUser.getPassword());

        newUser = as.createNewUser(login, password);

        assertNull(newUser);
    }

    @Test
    public void testGetAllUsers() throws Exception {
        AccountService as = new AccountService();
        // Eventually it will break.
        UserProfile user1 = as.getUser("admin");
        UserProfile user2 = as.getUser("guest");

        assertNotNull(user1);
        assertNotNull(user2);

        Map<Long, UserProfile> map = new HashMap<>();
        map.put(user1.getId(), user1);
        map.put(user2.getId(), user2);

        assertEquals(map.values().toString(), as.getAllUsers().toString());
    }

    @Test
    public void testGetUserByID() throws Exception {
        AccountService as = new AccountService();
        UserProfile user = as.getUser("admin");

        assertNotNull(user);

        Long id = user.getId();

        assertEquals(user, as.getUser(id));

        id = -1L;   // Should not happen to have.

        assertNull(as.getUser(id));
    }

    @Test
    public void testGetUserByLogin() throws Exception {
        AccountService as = new AccountService();
        final String login = "login";
        final String password = "password";

        UserProfile newUser = as.createNewUser(login, password);

        assertNotNull(newUser);

        UserProfile user = as.getUser(login);

        assertEquals(newUser, user);

        user = as.getUser("");

        assertNull(user);
    }

    @Test
    public void testDeleteUser() throws Exception {
        AccountService as = new AccountService();
        UserProfile user = as.getUser("admin");

        assertNotNull(user);

        Long id = user.getId();
        as.deleteUser(id);

        assertNull(as.getUser(id));

        id = -1L;
        as.deleteUser(id);

        assertNull(as.getUser(id));
    }
}