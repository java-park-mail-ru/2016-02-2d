package rest;

import com.sun.istack.internal.Nullable;
import main.TokenManager;
import main.database.UserProfileData;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicLong;

public class UserProfile {

    public UserProfile(UserProfileData userProfileData) {
        data = userProfileData;
        sessionID = null;
    }

    @NotNull
    public String getLogin() {
        return data.getLogin();
    }

    @NotNull
    public String getPassword() {
        return data.getPassword();
    }

    public void setPassword(@NotNull String password) {
        data.setPassword(password);
    }

    public long getId() {
        return data.getId();
    }

    public int getScore() {
        return data.getScore();
    }

    public void setScore(int newScore) {
        data.setScore(newScore);
    }

    public JSONObject toJson(){
        return new JSONObject().put("id", data.getId()).put("login", data.getLogin()).put("score", data.getScore());
    }

    @NotNull
    public String getSessionID() {
        if (sessionID == null)
            sessionID = TokenManager.getNewRandomSessionID(data.getLogin(), data.getId(), data.getScore(), data.getPassword());
        return sessionID;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash *= data.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserProfile ) {
            final UserProfile another = (UserProfile) obj;
            return (data.equals(another.data));
        }
        else return false;
    }

    public UserProfileData getData() {
        return data;
    }


    @Nullable
    private String sessionID;
    @NotNull
    private UserProfileData data;
}
