package bomberman.service;

import bomberman.mechanics.World;
import bomberman.mechanics.interfaces.WorldType;
import rest.UserProfile;
import org.eclipse.jetty.websocket.api.Session;

import java.util.HashMap;
import java.util.Map;

public class Room {

    public Room(){}

    public Room(int overrideCapacity) {
        capacity = overrideCapacity;
    }

    public void createNewWorld(WorldType type)
    {
        world = new World(type, playerMap.size());
    }

    public int getMaximalCapacity() {
        return capacity;
    }

    public int getCurrentCapacity() {
        return playerMap.size();
    }

    public boolean isFilled()
    {
        return playerMap.size() >= capacity;
    }

    public boolean isEmpty() {
        return playerMap.isEmpty();
    }

    public void insertPlayer(UserProfile user, Session session) {
        int newID = world.getBombermenIDs()[playerMap.size()];
        playerMap.put(newID, user);
        reversePlayerMap.put(user, newID);
        websocketMap.put(user, session);
    }

    public boolean hasPlayer(UserProfile user) {
        return playerMap.containsValue(user);
    }

    public void removePlayer(UserProfile user) {
        if (reversePlayerMap.containsKey(user)) {
            int bombermanID = reversePlayerMap.get(user);
            playerMap.remove(bombermanID);
            reversePlayerMap.remove(user);
            websocketMap.remove(user);
        }
    }

    // I can't determine hashCode and equals methods. :(

    private int capacity = DEFAULT_CAPACITY;
    private final Map<Integer, UserProfile> playerMap = new HashMap<>();
    private final Map<UserProfile, Integer> reversePlayerMap = new HashMap<>();
    private final Map<UserProfile, Session> websocketMap = new HashMap<>();

    World world;

    private static final int DEFAULT_CAPACITY = 4;
}
