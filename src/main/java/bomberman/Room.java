package bomberman;

import bomberman.interfaces.WorldType;

import java.util.HashMap;
import java.util.Map;

public class Room {

    public void createNewWorld(WorldType type)
    {
        world = new World(type, playerMap.size());
    }

    final Map<Integer, Player> playerMap = new HashMap<>();
    World world;
}
