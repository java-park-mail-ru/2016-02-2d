package main;

import main.accountservice.AccountService;
import main.config.Context;
import rest.Sessions;
import rest.Users;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@ApplicationPath("api/")
public class RestApplication extends Application {

    public RestApplication() {
        this.context = Main.context;
        final AccountService accountService = (AccountService) context.get(AccountService.class);
        final Map<String, String> properties = (Map<String, String>) context.get(Properties.class);

        objects.add(new Users(accountService, properties.get("static_path"), Integer.parseInt(properties.get("userpic_width")), Integer.parseInt(properties.get("userpic_height"))));
        objects.add(new Sessions(accountService));
    }

    @Override
    public Set<Object> getSingletons() {
        return objects;
    }


    //@Inject
    private Context context;

    private final HashSet<Object> objects = new HashSet<>();
}
