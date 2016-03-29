package main;

import main.database.DataBase;
import org.junit.Test;
import rest.UserProfile;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DataBaseTest {

    @Test
    public void testAddUser() throws Exception {
        DataBase db = new DataBase();
        UserProfile user =  db.addUser("login", "password");

        assertNotNull(user);
        assertEquals(db.getById(user.getId()), db.getByLogin(user.getLogin()));

        user = db.addUser("login", "password");

        assertNull(user);
    }

    @Test
    public void testDeleteUser() throws Exception {
        DataBase db = new DataBase();
        UserProfile user =  db.addUser("login", "password");

        assertNotNull(user);

        String login = user.getLogin();
        long id = user.getId();

        db.deleteUser(id);

        assertNull(db.getById(id));
        assertNull(db.getByLogin(login));
    }

    @Test
    public void testGetUsers() throws Exception {
        DataBase db = new DataBase();
        UserProfile user1 = db.addUser("login", "password");
        UserProfile user2 = db.addUser("user1", "user1");

        assertNotNull(user1);
        assertNotNull(user2);

        Map<Long, UserProfile> map = new HashMap<>();
        map.put(user1.getId(), user1);
        map.put(user2.getId(), user2);

        assertEquals(map.values().toString(), db.getUsers().toString());
    }

    @Test
    public void testContainsID() throws Exception {
        DataBase db = new DataBase();
        UserProfile user = db.addUser("login", "password");

        assertNotNull(user);
        long id = user.getId();

        assertEquals(true, db.containsID(id));

        id = -1L;

        assertEquals(false, db.containsID(id));

    }
}