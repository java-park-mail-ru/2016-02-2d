package bomberman.service;

import main.accountservice.AccountService;
import main.config.Context;
import org.json.JSONObject;
import rest.UserProfile;
import rest.WebErrorManager;

public class ReceivedMessageHandler /*implements Runnable*/ {
    public ReceivedMessageHandler(Room userRoom, JSONObject jsonMessage, Context globalContext) {
        room = userRoom;
        message = jsonMessage;
        context = globalContext;
    }

    public boolean execute() {
        final String messageType = message.getString("type");
        if (messageType.equals("object_changed")) {
            if (WebErrorManager.showFieldsNotPresent(message, "id", "x", "y") != null)
                return false;

            return doIfUserExists((user) -> room.scheduleBombermanMovement(user, message.getInt("x"), message.getInt("y")));
        }
        if (messageType.equals("user_state_changed")) {
            if (WebErrorManager.showFieldsNotPresent(message, "id", "isReady", "contentLoaded") != null)
                return false;

            return doIfUserExists((user) -> room.updatePlayerState(user, message.getBoolean("isReady"), message.getBoolean("contentLoaded")));
        }
        if (messageType.equals("chat_message")) {
            if (WebErrorManager.showFieldsNotPresent(message, "user_id", "text") != null)
                return false;

            room.broadcast(message.toString());

            return true;
        }
        return false;
    }

    private boolean doIfUserExists(Executor action)
    {
        final AccountService accountService = (AccountService) context.get(AccountService.class);
        final UserProfile user = accountService.getUser(message.getLong("id"));

        if (user == null)
            return false;
        else
            action.execute(user);
        return true;
    }

    private interface Executor {
        void execute(UserProfile user);
    }

    private final Room room;
    private final JSONObject message;
    private final Context context;
}
