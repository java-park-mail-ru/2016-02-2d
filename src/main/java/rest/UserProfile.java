package rest;

import org.jetbrains.annotations.Nullable;
import main.UserTokenManager;
import main.databaseservice.UserProfileData;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class UserProfile {

    public UserProfile(@NotNull UserProfileData userProfileData) {
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
        final JSONObject description = new JSONObject().put("id", data.getId()).put("login", data.getLogin()).put("score", data.getScore());
        if (getUserpicPath() == null)
            description.put("userpic_path", JSONObject.NULL);
        else
            description.put("userpic_path", getUserpicPath());

        return description;
    }

    @NotNull
    public String getSessionID() {
        if (sessionID == null)
            sessionID = UserTokenManager.getNewRandomSessionID(data.getLogin(), data.getId(), data.getScore(), data.getPassword());
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

    @NotNull
    public UserProfileData getData() {
        return data;
    }

    @Nullable
    public String getUserpicPath() {
        return data.getUserpicPath();
    }

    public void setUserpicPath(String path) {
        data.setUserpicPath(path);
    }


    @Nullable
    private String sessionID;
    @NotNull
    private final UserProfileData data;
}
