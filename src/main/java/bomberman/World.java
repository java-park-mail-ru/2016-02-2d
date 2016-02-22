package bomberman;

import bomberman.interfaces.*;

import java.util.LinkedList;
import java.util.Queue;

public class World implements EventStashable, UniqueIDManager, EventObtainable {


    public World(WorldType type) {
        tileArray = WorldBuilderForeman.getWorldBuilderInstance(type).getITileArray(this);
    }

    public World(WorldType type, int x, int y) {
        tileArray = WorldBuilderForeman.getWorldBuilderInstance(type).getITileArray(y, x, this);
    }

    @Override
    public void addWorldEvent(WorldEvent worldEvent) {
        newEventQueue.add(worldEvent);
    }

    @Override
    public int getNextID() {
        previousNextID++;
        return previousNextID;
    }

    @Override
    public Queue<WorldEvent> getFreshEvents() {
        Queue<WorldEvent> newQueue = new LinkedList<>(processedEventQueue);     // Java has static typing? For real?
        processedEventQueue.clear();
        return newQueue;
    }

    Queue<WorldEvent> newEventQueue;       // Here are new events are stashed
    Queue<WorldEvent> processedEventQueue; // State describer will take events from this list.

    private int previousNextID = 0;
    private ITile[][] tileArray;

}
