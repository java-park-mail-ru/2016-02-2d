package bomberman;

import bomberman.interfaces.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class World implements EventStashable, UniqueIDManager, EventObtainable {


    public World(WorldType type, int numberOfPlayers) {
        final IWorldBuilder builder = WorldBuilderForeman.getWorldBuilderInstance(type);

        tileArray = builder.getITileArray(this, this);
        spawnBombermenRegisterTilesAndSetWorldReady(numberOfPlayers, builder.getBombermenSpawns());
    }

    public World(WorldType type, int numberOfPlayers, int width, int height) {
        final IWorldBuilder builder = WorldBuilderForeman.getWorldBuilderInstance(type);

        tileArray = builder.getITileArray(height, width, this, this);
        spawnBombermenRegisterTilesAndSetWorldReady(numberOfPlayers, builder.getBombermenSpawns());
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
        final Queue<WorldEvent> newQueue = new LinkedList<>(processedEventQueue);     // Java has static typing? For real?
        processedEventQueue.clear();
        return newQueue;
    }

    public boolean isWorldReady() {
        return isWorldReady;
    }

    // For linking to Players via Room
    public int[] getBombermenIDs(){
        final int[] ids = new int[bombermen.size()];
        int i = 0;

        for (Bomberman bomberman : bombermen)
            ids[i++] = bomberman.getID();

        return ids;
    }

    private void spawnBombermen(int amount, float[][] locations){
        if (locations.length < amount)
            throw new ArrayIndexOutOfBoundsException();
        for (int i = 0; i < amount; ++i) {
            final Bomberman newBomberman = new Bomberman(getNextID());
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

    // Dirty hack to avoid copy-paste
    private void spawnBombermenRegisterTilesAndSetWorldReady(int numberOfPlayers, float[][] spawnLocations)
    {
        spawnBombermen(numberOfPlayers, spawnLocations);
        registerNewTiles();
        isWorldReady = true;
    }

    Queue<WorldEvent> newEventQueue;       // Here are new events are stashed
    Queue<WorldEvent> processedEventQueue; // State describer will take events from this list.

    private int previousNextID = 0;
    private final ITile[][] tileArray;
    private boolean isWorldReady = false;
    private boolean shouldSelfUpdate = false;
    private ArrayList<Bomberman> bombermen;



}
