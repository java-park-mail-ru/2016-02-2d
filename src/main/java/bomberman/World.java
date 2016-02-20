package bomberman;

import bomberman.interfaces.EventStashable;
import bomberman.interfaces.UniqueIDManager;

import java.util.HashSet;

public class World implements EventStashable, UniqueIDManager{


    public World() {
        previousNextID = 0;
    }

    @Override
    public void addWorldEvent(WorldEvent worldEvent) {
        newEventList.add(worldEvent);
    }

    @Override
    public int getNextID() {
        previousNextID++;
        return previousNextID;
    }

    HashSet<WorldEvent> newEventList;       // Here are new events are stashed
    HashSet<WorldEvent> processedEventList; // State describer will take events from this list.

    private int previousNextID;

}
