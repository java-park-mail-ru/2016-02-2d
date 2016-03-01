package main;

import rest.UserProfile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Павел on 27.02.2016.
 */
public class DataBase {
    private Map<String, UserProfile> LoginUsers = new HashMap<>();
    private Map<Long, UserProfile> IDUsers = new HashMap<>();

    public UserProfile getById(Long id){
        return IDUsers.get(id);
    }

    public UserProfile getByLogin(String name){

        return LoginUsers.get(name);
    }

    public boolean containsID (Long id){
       return IDUsers.containsKey(id);
    }

    public boolean containsLogin (String name){
        return IDUsers.containsKey(name);
    }

    public boolean addUser(String name, String pass){
        if (containsLogin(name)) return false;
        UserProfile newUser = new UserProfile(name, pass);
        LoginUsers.put(name, newUser);
        IDUsers.put(newUser.getId(), newUser);
        return true;
    }

    public Collection<UserProfile> getUsers() {
        return IDUsers.values();
    }

    public void deleteUser(Long id){
        UserProfile deletingUser = IDUsers.get(id);
        IDUsers.remove(id);
        LoginUsers.remove(deletingUser.getLogin());
    }


}
