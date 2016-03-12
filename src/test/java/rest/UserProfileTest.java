package rest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

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
}