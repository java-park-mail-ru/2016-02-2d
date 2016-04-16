package bomberman.service;

import main.accountservice.AccountService;
import main.config.Context;
import org.json.JSONObject;
import rest.UserProfile;

import javax.inject.Inject;

public class ReceivedMessageHandler /*implements Runnable*/ {
    @Inject
    private Context context;

    public ReceivedMessageHandler(Room userRoom, JSONObject jsonMessage) {
        room = userRoom;
        message = jsonMessage;
    }

    public boolean execute() {
        final String messageType = message.getString("type");
        if (messageType.equals("object_changed")) {
            return true;
        }
        if (messageType.equals("user_state_changed")) {
            return true;
        }
        /*if (messageType.equals("user_left")) {
            final AccountService accountService = (AccountService) context.get(AccountService.class);
            room.removePlayer(accountService.getUser(message.getLong("id")));

            room.broadcast(message.toString());
            return true;
        }*/
        if (messageType.equals("chat_message")) {
            room.broadcast(message.toString());
            return true;
        }
        return false;
    }

    private final Room room;
    private final JSONObject message;
}
