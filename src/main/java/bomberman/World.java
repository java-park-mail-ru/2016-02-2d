package bomberman;

import bomberman.interfaces.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class World implements EventStashable, UniqueIDManager, EventObtainable {


    public World(WorldType type, int numberOfPlayers) {
        IWorldBuilder builder = WorldBuilderForeman.getWorldBuilderInstance(type);

        tileArray = builder.getITileArray(this);
        spawnBombermen(numberOfPlayers, builder.getBombermenSpawn());
        registerNewTiles();
        isWorldReady = true;
    }

    public World(WorldType type, int numberOfPlayers, int width, int height) {
        IWorldBuilder builder = WorldBuilderForeman.getWorldBuilderInstance(type);

        tileArray = builder.getITileArray(height, width, this);
        spawnBombermen(numberOfPlayers, builder.getBombermenSpawn());               // Copy-paste from this line. Java lacks parameters by-default. :(
        registerNewTiles();
        isWorldReady = true;
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

    public boolean isWorldReady() {
        return isWorldReady;
    }

    // For linking to Players via Room
    public int[] getBombermenIDs(){
        int[] ids = new int[bombermen.size()];
        int i = 0;

        for (Bomberman bomberman : bombermen)
            ids[i++] = bomberman.getID();

        return ids;
    }

    private void spawnBombermen(int amount, float[][] locations){
        if (locations.length < amount)
            throw new ArrayIndexOutOfBoundsException();
        for (int i = 0; i < amount; ++i) {
            Bomberman newBomberman = new Bomberman(getNextID());
            bombermen.add(newBomberman);
            processedEventQueue.add(new WorldEvent(EventType.ENTITY_UPDATED, newBomberman.getType(), newBomberman.getID()));
        }

    }

    // Run only once at the very beginning
    private void registerNewTiles() {
        for (ITile[] row : tileArray)
            for(ITile tile : row)
                if (tile != null)
                    processedEventQueue.add(new WorldEvent(EventType.TILE_SPAWNED, tile.getType(), tile.getID()));


    }

    Queue<WorldEvent> newEventQueue;       // Here are new events are stashed
    Queue<WorldEvent> processedEventQueue; // State describer will take events from this list.

    private int previousNextID = 0;
    private ITile[][] tileArray;
    private boolean isWorldReady = false;
    private ArrayList<Bomberman> bombermen;

}
