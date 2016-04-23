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

            // TODO: Logic here

            return true;
        }
        if (messageType.equals("user_state_changed")) {
            if (WebErrorManager.showFieldsNotPresent(message, "id", "isReady", "contentLoaded") != null)
                return false;

            final AccountService accountService = (AccountService) context.get(AccountService.class);
            final UserProfile user = accountService.getUser(message.getLong("id"));

            if (user == null)
                return false;
            else
                room.updatePlayerState(user, message.getBoolean("isReady"), message.getBoolean("contentLoaded"));
            return true;
        }
        if (messageType.equals("chat_message")) {
            if (WebErrorManager.showFieldsNotPresent(message, "user_id", "text") != null)
                return false;

            room.broadcast(message.toString());

            return true;
        }
        return false;
    }

    private final Room room;
    private final JSONObject message;
    private final Context context;
}
