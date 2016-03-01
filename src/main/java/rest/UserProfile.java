package rest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicLong;

public class UserProfile {

    public UserProfile() {
        login = "";
        password = "";
        email = "";
        id = ID_GENETATOR.getAndIncrement();
    }

    public UserProfile(@NotNull String newLogin, @NotNull String newPassword, @NotNull String newEmail) {
        login = newLogin;
        password = newPassword;
        email = newEmail;
        id = ID_GENETATOR.getAndIncrement();
    }

    @NotNull
    public String getLogin() {
        return login;
    }

    public void setLogin(@NotNull String login) {
        this.login = login;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NotNull String password) {
        this.password = password;
    }

    public long getId() {
        return id;
    }

    @NotNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NotNull String email) {
        this.email = email;
    }

    public JSONObject toJson(){
        return new JSONObject().put("id", getId()).put("login", getLogin()).put("email", getEmail());
    }

    @NotNull
    private String login;
    @NotNull
    private String password;
    @NotNull
    private String email;
    private long id;

    private static final AtomicLong ID_GENETATOR = new AtomicLong(0);
}
