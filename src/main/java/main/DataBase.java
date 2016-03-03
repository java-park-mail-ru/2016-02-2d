package main;

import com.sun.istack.internal.Nullable;
import rest.UserProfile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DataBase {

    // Create
    public void addUser(String login, String password, String email){
        if (containsLogin(login))
            return;
        final UserProfile newUser = new UserProfile(login, password, email);
        loginToUser.put(login, newUser);
        idToUser.put(newUser.getId(), newUser);
    }

    // Read, Update
    @Nullable
    public UserProfile getById(Long id){
        return idToUser.get(id);
    }

    @Nullable
    public UserProfile getByLogin(String name){

        return loginToUser.get(name);
    }

    public Collection<UserProfile> getUsers() {
        return idToUser.values();
    }

    public boolean containsID (Long id){
       return idToUser.containsKey(id);
    }

    public boolean containsLogin (String name){
        return loginToUser.containsKey(name);
    }

    // Delete
    public void deleteUser(Long id){
        final UserProfile deletingUser = idToUser.get(id);
        idToUser.remove(id);
        loginToUser.remove(deletingUser.getLogin());
    }



    private final Map<String, UserProfile> loginToUser = new HashMap<>();
    private final Map<Long, UserProfile> idToUser = new HashMap<>();

}
