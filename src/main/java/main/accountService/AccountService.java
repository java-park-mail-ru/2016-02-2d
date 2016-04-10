package main.accountService;

import main.databaseService.DataBaseService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.Collection;

public interface AccountService {
    void loginUser(@NotNull UserProfile user);

    @Nullable
    UserProfile getBySessionID(@Nullable String sessionID);

    boolean hasSessionID(@Nullable String sessionID);

    boolean logoutUser(@Nullable String sessionID);

    @Nullable
    UserProfile createNewUser(@NotNull String login, @NotNull String password);

    @Nullable
    Collection<UserProfile> getAllUsers();

    @Nullable
    UserProfile getUser(@NotNull Long id);

    @Nullable
    UserProfile getUser(@NotNull String login);

    void deleteUser(@NotNull Long id);

    void updateUser(UserProfile user);

    void changeDB(DataBaseService dataBaseService);
}
