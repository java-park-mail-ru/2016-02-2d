package main;

import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DataBase {

    @Nullable
    public UserProfile addUser(String login, String password){
        if (containsLogin(login))
            return null;
        final UserProfile newUser = new UserProfile(login, password);
        loginToUser.put(login, newUser);
        idToUser.put(newUser.getId(), newUser);
        return newUser;
    }

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

    public void deleteUser(Long id){
        final UserProfile deletingUser = idToUser.get(id);
        idToUser.remove(id);
        loginToUser.remove(deletingUser.getLogin());
    }


    private final Map<String, UserProfile> loginToUser = new HashMap<>();
    private final Map<Long, UserProfile> idToUser = new HashMap<>();

}
