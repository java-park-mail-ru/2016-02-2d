package main.databaseservice;

import javax.persistence.*;
import java.io.Serializable;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "users")
public class UserProfileData implements Serializable {

    public UserProfileData(@NotNull String newLogin, @NotNull String newPasssword) {
        login = newLogin;
        password = newPasssword;
    }

    public UserProfileData() {}

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

    public void setId(long id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int newScore) {
        score = newScore;
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash *= login.hashCode();
        hash *= id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserProfileData) {
            final UserProfileData another = (UserProfileData) obj;
            return (another.id == id && another.login.equals(login));
        } else return false;
    }


    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id = -1;

    @NotNull
    @Column(name = "user_name")
    private String login = "ERROR! REPORT IF YOU SEE THIS.";

    @NotNull
    @Column(name = "user_password")
    private String password = "ERROR! REPORT IF YOU SEE THIS.";

    @Column(name = "user_score")
    private int score = 0;

    private static final long serialVersionUID = -8706689714326132798L;

    //private Integer hash = null;
}
