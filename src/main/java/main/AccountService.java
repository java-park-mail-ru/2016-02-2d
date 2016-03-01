package main;

import com.sun.istack.internal.Nullable;
import rest.UserProfile;

import javax.ws.rs.core.Cookie;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AccountService {

    // Store some dummies.
    public AccountService() {
        registeredUsers.addUser("admin", "admin", "admin@admin");
        registeredUsers.addUser("guest", "12345", "lol@memes");
    }

    //
    // /api/sessions
    //

    // Create
    @Nullable
    public UserProfile loginUser(Cookie cookie, long id){
        if (activeUsers.put(cookie, id) != null)
            return registeredUsers.getById(id);
        return null;
    }

    // Read, Update
    @Nullable
    public UserProfile getByCookie(Cookie cookie){
        if (activeUsers.containsKey(cookie))
            return registeredUsers.getById(activeUsers.get(cookie));
        return null;
    }
    

    // Delete
    public void logoutUser(Cookie cookie){
        activeUsers.remove(cookie);
    }

    //
    // /api/user
    //

    // Create
    public boolean createNewUser(String login, String password, String email) {
        if (registeredUsers.containsLogin(login))
            return false;
        registeredUsers.addUser(login, password, email);
        return true;
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



    private Map<Cookie, Long> activeUsers = new HashMap<>();
    private DataBase registeredUsers = new DataBase();
}
