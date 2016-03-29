package rest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

public class UserProfileTest {

    @Test
    public void testToJson() throws JSONException {
        long baseID = new UserProfile("", "").getId();
        JSONObject userJson = new UserProfile("", "").toJson();
        JSONObject fieldsJson = new JSONObject().put("id", baseID + 1).put("login", "").put("score", 0);

        assertEquals(fieldsJson.toString(), userJson.toString());
    }

    @Test
    public void testGetIncrementalId() {
        UserProfile user1 = new UserProfile("", "");
        UserProfile user2 = new UserProfile("", "");
        long baseID = user1.getId();

        assertEquals(baseID + 1, user2.getId());
    }

    @Test
    public void testGetDifferentSessionID() {
        UserProfile user1 = new UserProfile("", "");
        UserProfile user2 = new UserProfile("", "");

        assertNotSame(user1.getSessionID(), user2.getSessionID());
    }

    @Test
    public void testGetPassword() throws Exception {
        final String password = "password";
        UserProfile user = new UserProfile("login", password);

        assertEquals(password, user.getPassword());
    }

    @Test
    public void testSetPassword() throws Exception {
        String password = "password";
        UserProfile user = new UserProfile("login", password);
        password = "login";

        assertNotSame(password, user.getPassword());

        user.setPassword(password);

        assertEquals(password, user.getPassword());

    }

    @Test
    public void testGetScore() throws Exception {
        final int score = 0;
        UserProfile user = new UserProfile("login", "password");

        assertEquals(score, user.getScore());
    }

    @Test
    public void testSetScore() throws Exception {
        final int score = 1;
        UserProfile user = new UserProfile("login", "password");

        assertNotSame(score, user.getScore());

        user.setScore(score);

        assertEquals(score, user.getScore());
    }

    @Test
    public void testHashCode() throws Exception {
        UserProfile user1 = new UserProfile("admin", "admin");
        UserProfile user2 = new UserProfile("guest", "1234");

        assertEquals(user1.hashCode(), user1.hashCode());
        assertNotSame(user1.hashCode(), user2.hashCode());

        Map<Long, UserProfile> map = new HashMap<>();

        map.put(user1.getId(), user1);

        assertEquals(false, map.isEmpty());
        assertEquals(user1, map.get(user1.getId()));

        map.put(user2.getId(), user2);

        assertEquals(false, map.isEmpty());
        assertEquals(user1, map.get(user1.getId()));
        assertEquals(user2, map.get(user2.getId()));
        assertNotSame(user1, map.get(user2.getId()));
        assertNotSame(user2, map.get(user1.getId()));

    }

    @Test
    public void testEquals() throws Exception {
        UserProfile user1 = new UserProfile("admin", "admin");
        UserProfile user2 = new UserProfile("guest", "1234");

        assertEquals(user1, user1);
        assertEquals(false, user2.equals(user1));
    }
}