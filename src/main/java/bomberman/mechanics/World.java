package bomberman.mechanics;

import bomberman.mechanics.interfaces.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class World implements EventStashable, UniqueIDManager, EventObtainable {

    public World(String worldType, int numberOfPlayers, Runnable actionOnWorldReady) {
        final IWorldBuilder builder = WorldBuilderForeman.getWorldBuilderInstance(worldType);

        actionOnceWorldIsReady = actionOnWorldReady;
        tileArray = builder.getITileArray(this, this);
        spawnLocations = builder.getBombermenSpawns();
        registerNewTiles();
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

    public void doSomethingIfWorldReady() {
        if (areBombermenSpawned && areTilesPositioned && !hasWorldReadyAcionFired){
            hasWorldReadyAcionFired = true;
            actionOnceWorldIsReady.run();
        }
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
            processedEventQueue.add(new WorldEvent(EventType.ENTITY_UPDATED, newBomberman.getType(), newBomberman.getID(), spawnLocations[i][0], spawnLocations[i][1]));
        }
        areBombermenSpawned = true;
    }

    // Run only once at the very beginning
    private void registerNewTiles() {
        for (int y = 0; y < tileArray.length; ++y) {
            final ITile[] row = tileArray[y];
            for (int x = 0; x < tileArray[y].length; ++x) {
                final ITile tile = row[x];
                if (tile != null)
                    processedEventQueue.add(new WorldEvent(EventType.TILE_SPAWNED, tile.getType(), tile.getID(), x, y));
            }
        }
        areTilesPositioned = true;
    }

    Queue<WorldEvent> newEventQueue;       // Here are new events are stashed
    Queue<WorldEvent> processedEventQueue; // State describer will take events from this list.

    private final AtomicInteger uidManager = new AtomicInteger(0);

    private final ITile[][] tileArray;
    private final ArrayList<Bomberman> bombermen = new ArrayList<>(4);

    private final float[][] spawnLocations;
    private boolean areBombermenSpawned = false;
    private boolean areTilesPositioned = false;
    private boolean hasWorldReadyAcionFired = false;

    private boolean shouldSelfUpdate = false;
    private final Runnable actionOnceWorldIsReady;
}
