package rest;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author esin88
 */
public class UserProfile {
    private static final AtomicLong ID_GENETATOR = new AtomicLong(0);
    @NotNull
    private String login;
    @NotNull
    private String password;
    private long id;

    public UserProfile() {
        login = "";
        password = "";
        id = ID_GENETATOR.getAndIncrement();
    }

    public UserProfile(@NotNull String login, @NotNull String password) {
        this.login = login;
        this.password = password;
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

    @NotNull
    public long getId() {
        return id;
    }
}
