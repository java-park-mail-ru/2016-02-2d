package bomberman.mechanics;

import bomberman.mechanics.interfaces.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class World implements EventStashable, UniqueIDManager, EventObtainable {

    public World(WorldType type, int numberOfPlayers) {
        final IWorldBuilder builder = WorldBuilderForeman.getWorldBuilderInstance(type);

        tileArray = builder.getITileArray(this, this);
        spawnLocations = builder.getBombermenSpawns();
        registerNewTiles();
        isWorldReady = true;
    }

    public World(WorldType type, int numberOfPlayers, int width, int height) {
        final IWorldBuilder builder = WorldBuilderForeman.getWorldBuilderInstance(type);

        tileArray = builder.getITileArray(height, width, this, this);
        spawnLocations = builder.getBombermenSpawns();
        registerNewTiles();
        isWorldReady = true;
    }

    @Override
    public void addWorldEvent(WorldEvent worldEvent) {
        newEventQueue.add(worldEvent);
    }

    @Override
    public int getNextID() {
        return uidManager.getAndIncrement();
    }

    @Override
    public Queue<WorldEvent> getFreshEvents() {
        final Queue<WorldEvent> newQueue = new LinkedList<>(processedEventQueue);
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

    public void spawnBombermen(int amount){
        if (spawnLocations.length < amount)
            throw new ArrayIndexOutOfBoundsException();
        for (int i = 0; i < amount; ++i) {
            final Bomberman newBomberman = new Bomberman(getNextID());
            newBomberman.setCoordinates(spawnLocations[i]);
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

    private AtomicInteger uidManager = new AtomicInteger(0);
    private final ITile[][] tileArray;
    private final float[][] spawnLocations;
    private boolean isWorldReady = false;
    private boolean shouldSelfUpdate = false;
    private ArrayList<Bomberman> bombermen = new ArrayList<>(4);



}
