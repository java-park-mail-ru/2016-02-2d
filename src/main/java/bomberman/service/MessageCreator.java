package bomberman.service;

import org.json.JSONObject;
import rest.UserProfile;

public class MessageCreator {
    public static String createUserJoinedMessage(UserProfile joinee) {
        final JSONObject messageTemplate = new JSONObject();

        messageTemplate.put("id", joinee.getId());
        messageTemplate.put("isReady", false);
        messageTemplate.put("name", joinee.getLogin());
        messageTemplate.put("score", joinee.getScore());
        messageTemplate.put("userpic_path", JSONObject.NULL);

        return messageTemplate.toString();
    }
}
