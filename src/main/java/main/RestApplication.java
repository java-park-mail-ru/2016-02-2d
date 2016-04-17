package main;

import main.accountservice.AccountService;
import main.config.Context;
import rest.Sessions;
import rest.Users;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("api")
public class RestApplication extends Application {

    public RestApplication() {
        final AccountService accountService = (AccountService) Main.getGlobalContext().get(AccountService.class);
        objects.add(new Users(accountService));
        objects.add(new Sessions(accountService));
    }

    @Override
    public Set<Object> getSingletons() {
        return objects;
    }

    private final HashSet<Object> objects = new HashSet<>();
}
