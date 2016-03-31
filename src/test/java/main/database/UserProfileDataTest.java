package main.database;

import constants.Constants;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserProfileDataTest {

    @Before
    public void init() {
        user = new UserProfileData(Constants.USER_LOGIN, Constants.USER_PASSWORD);
        user.setId(Constants.USER_ID);
    }

    @Test
    public void testGetLogin() throws Exception {
        assertEquals(Constants.USER_LOGIN, user.getLogin());
    }

    @Test
    public void testSetLogin() throws Exception {
        final String newLogin = "aaa";
        user.setLogin(newLogin);

        assertEquals(newLogin, user.getLogin());
    }

    @Test
    public void testGetPassword() throws Exception {
        assertEquals(Constants.USER_PASSWORD, user.getPassword());
    }

    @Test
    public void testSetPassword() throws Exception {
        final String newPassword = "bbb";
        user.setPassword(newPassword);

        assertEquals(newPassword, user.getPassword());
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals(Constants.USER_PASSWORD, user.getPassword());
    }

    @Test
    public void testSetId() throws Exception {
        final long newID = 1;
        user.setId(newID);

        assertEquals(newID, user.getId());
    }

    @Test
    public void testGetScore() throws Exception {
        assertEquals(0, user.getScore());
    }

    @Test
    public void testSetScore() throws Exception {
        final int newScore = 1;
        user.setScore(newScore);

        assertEquals(newScore, user.getScore());
    }

    @Test
    public void testHashCode() throws Exception {
        final UserProfileData user2 = new UserProfileData(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotEquals(user.hashCode(), user2.hashCode());

        user2.setId(Constants.USER_ID);

        assertEquals(user.hashCode(), user2.hashCode());
    }

    @Test
    public void testEquals() throws Exception {
        assertNotEquals(user, null);

        final UserProfileData user2 = new UserProfileData(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotEquals(user, user2);

        user2.setId(Constants.USER_ID);

        assertEquals(user, user2);
    }

    private UserProfileData user;
}