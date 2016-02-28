package main;

import rest.UserProfile;

import javax.servlet.http.Cookie;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author esin88
 */
public class AccountService {
    private Map<Cookie, String> cookies = new HashMap<>();
    DataBase users = new DataBase();

    public AccountService() {
        users.addUser("admin", "admin");
        users.addUser("guest", "12345");
    }

    public Collection<UserProfile> getAllUsers() {
        return users.getUsers();
    }

    public boolean addUser(String userName, UserProfile userProfile) {

        if (users.containsLogin(userName))
            return false;
        users.addUser(userName, userProfile.getPassword());
        return true;
    }

    public UserProfile getUser(Long id) {
        return users.getById(id);
    }

    public UserProfile getUser(String login) {
        return users.getByLogin(login);
    }

    public boolean delUser (Long id) {

        if (users.containsID(id))
            return false;
        users.deleteUser(id);
        return true;
    }

    public void addNewCookie(Cookie cookie, String name){
        cookies.put(cookie, name);
    }

    public String getByCookie(String cookie){
        if(cookies.containsKey(cookie)) return cookies.get(cookie);
        else return null;
    }
}
