package main;

import constants.Constants;
import main.database.DataBaseHashMapImpl;
import org.junit.Before;
import org.junit.Test;
import rest.UserProfile;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AccountServiceImplTest {

    @Before
    public void init() {
        accountService = new AccountServiceImpl();
        accountService.changeDB(new DataBaseHashMapImpl());
    }

    @Test
    public void testLoginUser() throws Exception {
        UserProfile user = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        String sid = user.getSessionID();
        accountService.loginUser(user);

        assertEquals(user, accountService.getBySessionID(sid));

    }

    @Test
    public void testGetBySessionID() throws Exception {

        UserProfile user = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        String sid = user.getSessionID();
        accountService.loginUser(user);

        assertEquals(user, accountService.getBySessionID(sid));

        assertNull(accountService.getBySessionID(""));
    }

    @Test
    public void testHasSessionID() throws Exception {
        UserProfile user = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        String sid = user.getSessionID();

        assertEquals(false, accountService.hasSessionID(sid));

        accountService.loginUser(user);

        assertEquals(true, accountService.hasSessionID(sid));
    }

    @Test
    public void testLogoutUser() throws Exception {
        UserProfile user = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        String sid = user.getSessionID();

        assertEquals(false, accountService.logoutUser(sid));

        accountService.loginUser(user);

        assertEquals(true, accountService.logoutUser(sid));
    }

    @Test
    public void testCreateNewUser() throws Exception {
        UserProfile newUser = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(newUser);
        assertEquals(Constants.USER_LOGIN, newUser.getLogin());
        assertEquals(Constants.USER_PASSWORD, newUser.getPassword());

        newUser = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNull(newUser);
    }

    @Test
    public void testGetAllUsers() throws Exception {
        UserProfile user1 = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user1);

        Map<Long, UserProfile> map = new HashMap<>();
        map.put(user1.getId(), user1);

        assertEquals(map.values().toString(), accountService.getAllUsers().toString());
    }

    @Test
    public void testGetUserByID() throws Exception {
        UserProfile user = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        Long id = user.getId();

        assertEquals(user, accountService.getUser(id));

        id = Constants.USER_ID;   // Should not happen to have.

        assertNull(accountService.getUser(id));
    }

    @Test
    public void testGetUserByLogin() throws Exception {
        UserProfile newUser = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(newUser);

        UserProfile user = accountService.getUser(Constants.USER_LOGIN);

        assertEquals(newUser, user);

        user = accountService.getUser("");

        assertNull(user);
    }

    @Test
    public void testDeleteUser() throws Exception {
        UserProfile user = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        Long id = user.getId();
        accountService.deleteUser(id);

        assertNull(accountService.getUser(id));

        id = -1L;
        accountService.deleteUser(id);

        assertNull(accountService.getUser(id));
    }

    AccountService accountService;
}