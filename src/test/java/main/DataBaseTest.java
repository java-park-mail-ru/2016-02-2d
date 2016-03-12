package main;

import org.junit.Test;
import rest.UserProfile;

import static org.junit.Assert.*;

public class DataBaseTest {

    @Test
    public void testAddUser() throws Exception {
        DataBase db = new DataBase();
        UserProfile user =  db.addUser("login", "password");

        assertNotNull(user);
        assertEquals(db.getById(user.getId()), db.getByLogin(user.getLogin()));
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
}