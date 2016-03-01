package rest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicLong;

public class UserProfile {

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
    public String getEmail() {  // Yes, I doubt we will need it. It is question about API.
        return email;
    }

    public void setEmail(@NotNull String email) {
        this.email = email;
    }

    public JSONObject toJson(){
        return new JSONObject().put("id", id).put("login", login).put("email", email);
    }

    @NotNull
    private final String login;
    @NotNull
    private String password;
    @NotNull
    private String email;
    @SuppressWarnings("InstanceVariableNamingConvention")   // "id" is quite standart name.
    private final long id;

    private static final AtomicLong ID_GENETATOR = new AtomicLong(0);
}
