package main;

import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AccountService {

    // Store some dummies.
    public AccountService() {
        registeredUsers.addUser("admin", "admin").setScore(100);    // It won't.
        registeredUsers.addUser("guest", "12345");
    }

    //
    // /api/sessions
    //

    // Create
    public void loginUser(String sessionID, long id){
        activeUsers.put(sessionID, id);
    }

    // Read, Update
    @Nullable
    public UserProfile getBySessionID(String sessionID){
        if (activeUsers.containsKey(sessionID))
            return registeredUsers.getById(activeUsers.get(sessionID));
        return null;
    }
    

    // Delete
    public boolean logoutUser(String sessionID){
        if (!activeUsers.containsKey(sessionID))
            return false;
        activeUsers.remove(sessionID);
        return true;
    }

    //
    // /api/user
    //

    // Create
    @Nullable
    public UserProfile createNewUser(String login, String password) {
        if (registeredUsers.containsLogin(login))
            return null;
        return registeredUsers.addUser(login, password);
    }

    // Read, Update
    public Collection<UserProfile> getAllUsers() {
        return registeredUsers.getUsers();
    }

    @Nullable
    public UserProfile getUser(Long id) {
        return registeredUsers.getById(id);
    }

    @Nullable
    public UserProfile getUser(String login) {
        return registeredUsers.getByLogin(login);
    }

    // Delete
    public boolean deleteUser(Long id) {
        if (registeredUsers.containsID(id))
            return false;
        registeredUsers.deleteUser(id);
        return true;
    }



    private final Map<String, Long> activeUsers = new HashMap<>();
    private final DataBase registeredUsers = new DataBase();
}
