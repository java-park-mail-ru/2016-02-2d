package main;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AccountService {

    public AccountService() {
        //noinspection ConstantConditions
        registeredUsers.addUser("admin", "admin").setScore(100);    // It won't.
        registeredUsers.addUser("guest", "12345");
    }

    public void loginUser(@NotNull UserProfile user){
        activeUsers.put(user.getSessionID(), user.getId());
    }

    @Nullable
    public UserProfile getBySessionID(@Nullable String sessionID){
        if (activeUsers.containsKey(sessionID))
            return registeredUsers.getById(activeUsers.get(sessionID));
        return null;
    }

    public boolean hasSessionID(@Nullable String sessionID){
        return activeUsers.containsKey(sessionID);
    }

    public boolean logoutUser(@Nullable String sessionID){
        if (!activeUsers.containsKey(sessionID))
            return false;
        activeUsers.remove(sessionID);
        return true;
    }

    @Nullable
    public UserProfile createNewUser(@NotNull String login,@NotNull String password) {
        if (registeredUsers.containsLogin(login))
            return null;
        return registeredUsers.addUser(login, password);
    }

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

    public void deleteUser(@NotNull Long id) {
        if (!registeredUsers.containsID(id))
            return;
        registeredUsers.deleteUser(id);
    }


    private final Map<String, Long> activeUsers = new HashMap<>();
    private final DataBase registeredUsers = new DataBase();
}
