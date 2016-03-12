package rest;

import com.sun.istack.internal.Nullable;
import main.TokenManager;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicLong;

public class UserProfile {

    public UserProfile(@NotNull String newLogin, @NotNull String newPassword) {
        login = newLogin;
        password = newPassword;
        id = ID_GENETATOR.getAndIncrement();
        sessionID = null;   // Newly registered users are not active.
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

    public int getScore() {
        return score;
    }

    public void setScore(int newScore) {
        score = newScore;
    }

    public JSONObject toJson(){
        return new JSONObject().put("id", id).put("login", login).put("score", score);
    }

    @NotNull
    public String getSessionID() {
        if (sessionID == null)
            sessionID = TokenManager.getNewRandomSessionID(login, id, score);
        return sessionID;
    }

    @NotNull
    private final String login;
    @NotNull
    private String password;
    @Nullable
    private String sessionID;
    @SuppressWarnings("InstanceVariableNamingConvention")   // "id" is quite standart name.
    private final long id;
    private int score = 0;

    private static final AtomicLong ID_GENETATOR = new AtomicLong(0);
}
