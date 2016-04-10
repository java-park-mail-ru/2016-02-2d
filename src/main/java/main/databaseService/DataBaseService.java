package main.database;

import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.Collection;
public interface DataBaseService {

    void save(UserProfileData dataSet);

    @Nullable
    UserProfile getById(long id);

    @Nullable
    UserProfile getByLogin(String name);

    @Nullable
    Collection<UserProfile> getUsers();

    @Nullable
    UserProfile addUser(String login, String password);

    boolean containsID(Long id);

    boolean containsLogin(String name);

    void deleteUser(Long id);

}
