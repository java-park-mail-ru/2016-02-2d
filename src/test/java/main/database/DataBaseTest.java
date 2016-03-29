package main.database;

import constants.Constants;
import org.junit.Before;
import org.junit.Test;
import rest.UserProfile;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DataBaseTest {

    @Before
    public void init() {
        dataBase = new DataBaseRealImpl();
    }

    @Test
    public void testAddUser() throws Exception {

        UserProfile user = dataBase.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);
        assertEquals( dataBase.getById(user.getId()),  dataBase.getByLogin(user.getLogin()));

        user =  dataBase.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNull(user);
    }

    @Test
    public void testDeleteUser() throws Exception {
        
        final UserProfile user =   dataBase.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        final String login = user.getLogin();
        final long id = user.getId();

         dataBase.deleteUser(id);

        assertNull( dataBase.getById(id));
        assertNull( dataBase.getByLogin(login));
    }

    @Test
    public void testGetUsers() throws Exception {
        
        final UserProfile user1 =  dataBase.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);
        final UserProfile user2 =  dataBase.addUser("user1", "user1");

        assertNotNull(user1);
        assertNotNull(user2);

        final Map<Long, UserProfile> map = new HashMap<>();
        map.put(user1.getId(), user1);
        map.put(user2.getId(), user2);

        assertEquals(map.values().toString(),  dataBase.getUsers().toString());
    }

    @Test
    public void testContainsID() throws Exception {
        
        final UserProfile user =  dataBase.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);
        long id = user.getId();

        assertEquals(true,  dataBase.containsID(id));

        id = -1L;

        assertEquals(false,  dataBase.containsID(id));

    }

    @Test
    public void testSave() throws Exception {

    }

    @Test
    public void testGetById() throws Exception {

    }

    @Test
    public void testGetByLogin() throws Exception {

    }

    @Test
    public void testContainsLogin() throws Exception {

    }

    private DataBaseRealImpl dataBase;

}