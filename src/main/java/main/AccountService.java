package main;

import org.jetbrains.annotations.NotNull;
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
    public void loginUser(@NotNull UserProfile user){
        activeUsers.put(user.getSessionID(), user.getId());
    }

    // Read, Update
    @Nullable
    public UserProfile getBySessionID(@Nullable String sessionID){
        if (activeUsers.containsKey(sessionID))
            return registeredUsers.getById(activeUsers.get(sessionID));
        return null;
    }

    public boolean hasSessionID(@Nullable String sessionID){
        return activeUsers.containsKey(sessionID);
    }
    

    // Delete
    public boolean logoutUser(@Nullable String sessionID){
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
    public UserProfile createNewUser(@NotNull String login,@NotNull String password) {
        if (registeredUsers.containsLogin(login))
            return null;
        return registeredUsers.addUser(login, password);
    }

    // Read, Update
    @NotNull
    public Collection<UserProfile> getAllUsers() {
        return registeredUsers.getUsers();
    }

    @Nullable
    public UserProfile getUser(@NotNull Long id) {
        return registeredUsers.getById(id);
    }

    @Nullable
    public UserProfile getUser(@NotNull String login) {
        return registeredUsers.getByLogin(login);
    }

    // Delete
    public boolean deleteUser(@NotNull Long id) {
        if (registeredUsers.containsID(id))
            return false;
        registeredUsers.deleteUser(id);
        return true;
    }



    private final Map<String, Long> activeUsers = new HashMap<>();
    private final DataBase registeredUsers = new DataBase();
}
