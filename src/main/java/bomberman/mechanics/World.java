package bomberman.mechanics;

import antlr.collections.impl.Vector;
import bomberman.mechanics.interfaces.*;
import bomberman.service.MessageCreator;
import bomberman.service.TimeHelper;
import com.sun.javafx.geom.Vec2d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class World implements EventStashable, UniqueIDManager, EventObtainable {

    public World(String worldType, int numberOfPlayers, Runnable actionOnWorldReady) {
        final IWorldBuilder builder = WorldBuilderForeman.getWorldBuilderInstance(worldType);
        final Triplet<ITile[][], float[][], String> worldData = builder.getWorldData(this, this);

        actionOnceWorldIsReady = actionOnWorldReady;
        tileArray = worldData.getValue0();
        spawnLocations = worldData.getValue1();
        name = worldData.getValue2();
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

    // This is how timeline works:
    // |--------running---------|----------------------sleeping---------------------|
    // ↑                        ↑                                                   ↑
    // afterSleep0            afterRun1                                             afterSleep1
    // Total length of "running" and "sleeping" equals "FIXED_TIME_STEP"
    // If the "running" state continues longer than "FIXED_TIME_STEP", following happens:
    // |--------running1_starts-----------------------------------------------------+-running1_ends-|---running2------------|----------sleeping2-----------------|
    // ↑                                                                            ↑               ↑                       ↑                                    ↑
    // afterSleep0                                                    FIXED_TIME_STEP               |                       afterRun2                            afterSleep2
    //                                                                                              afterRun1 & afterSleep1
    public void update() {
        long afterSleep = TimeHelper.now();
        runGameCycle(FIXED_TIME_STEP);
        long afterRun = TimeHelper.now();

        long timeSpentWhileRunning = afterRun - afterSleep;
        logGameCycleTime(timeSpentWhileRunning);

        while (selfUpdatingEntities > 0) {
            final long timeToSleep = FIXED_TIME_STEP - timeSpentWhileRunning;
            TimeHelper.sleepFor(timeToSleep);
            afterSleep = TimeHelper.now();

            runGameCycle(FIXED_TIME_STEP);
            afterRun = TimeHelper.now();

            timeSpentWhileRunning = afterRun - afterSleep;
            logGameCycleTime(timeSpentWhileRunning);
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

    private void runGameCycle(long deltaT) {
        // foreach entity => etnity.update(delta)
        while (!newEventQueue.isEmpty())
        {
            final WorldEvent elderEvent = newEventQueue.remove();
            switch (elderEvent.getEventType()){
                case ENTITY_UPDATED:
                    processEntityUpdatedEvent(elderEvent, deltaT);
                    processedEventQueue.add(elderEvent);
                    break;
                case TILE_SPAWNED:
                    processTileSpawnedEvent(elderEvent);
                    processedEventQueue.add(elderEvent);
                    break;
                case TILE_REMOVED:
                    processTileRemovedEvent(elderEvent);
                    processedEventQueue.add(elderEvent);
                    break;
            }
        }
    }

    private void processEntityUpdatedEvent(WorldEvent event, float deltaT) {
        LOGGER.debug("Processing object_updated");
        // TODO: Bombermen movement
        // TODO: Bomberman bonus pickup
        tryMovingBomberman(event, deltaT);
    }

    private void processTileSpawnedEvent(WorldEvent event) {
        LOGGER.debug("Processing object_spawned");
        // TODO: Bomb placement
        // TODO: Bomb raycasting and destroying walls
    }

    private void processTileRemovedEvent(WorldEvent event) {
        LOGGER.debug("Processing object_destroyed");
        // TODO: Bomb explosion
        // TODO: Bomb Ray dissipation
        // TODO: Bonuses pickup?
    }

    // Hard maths. PM @Xobotun to get an explanation, don't be shy!
    private void tryMovingBomberman(WorldEvent event, float deltaT) {
        final int worldWidth = tileArray.length - 1;
        final int worldHeight = tileArray[0].length - 1;

        final Bomberman actor = bombermen.get(event.getEntityID());
        final boolean isMovingRight = event.getX() > 0;
        final boolean isMovingDown = event.getY() > 0;

        boolean shouldCheckXCorner = false;
        boolean shouldCheckYCorner = false;

        final float x = actor.getCoordinates()[0];
        final int ix = (int) Math.floor(x);
        final float y = actor.getCoordinates()[1];
        final int iy = (int) Math.floor(y);

        final float xSpeed = ((isMovingRight) ? 1 : -1) * actor.getMaximalSpeed() * (float)(event.getX() / Math.sqrt(event.getX() * event.getX() + event.getY() * event.getY()));
        final float ySpeed = ((isMovingDown) ? 1 : -1) * actor.getMaximalSpeed() * (float) (event.getY() / Math.sqrt(event.getX() * event.getX() + event.getY() * event.getY()));

        float predictedX = x + xSpeed * (deltaT / 1000);
        float predictedY = y + ySpeed * (deltaT / 1000);
        final float radius = Bomberman.DIAMETER / 2;

        final float xBoundary = (float) (Math.floor(x) + ((isMovingRight) ? 1 : 0));
        final float yBoundary = (float) (Math.floor(y) + ((isMovingDown) ? 1 : 0));

        if (predictedX - radius < 0)
            predictedX = radius;
        else if (predictedX + radius > worldWidth)
            predictedX = worldWidth - radius;
        else if (x - radius >= 1 && !isMovingRight && predictedX - radius < xBoundary) {
                final ITile leftTile = tileArray[iy][ix - 1];
                if (!leftTile.isPassable())
                    predictedX = xBoundary + radius;
                else shouldCheckXCorner = true; }
        else if (x + radius >= worldWidth - 1 && isMovingRight && predictedX + radius > xBoundary) {
            final ITile rightTile = tileArray[iy][ix + 1];
            if (!rightTile.isPassable())
                predictedX = xBoundary - radius;
            else shouldCheckXCorner = true; }

        if (predictedY - radius < 0)
            predictedY = radius;
        else if (predictedY + radius > worldHeight)
            predictedY = worldHeight - radius;
        else if (x - radius >= 1 && !isMovingDown && predictedY - radius < yBoundary) {
            final ITile upTile = tileArray[iy][ix - 1];
            if (!upTile.isPassable())
                predictedY = yBoundary + radius;
            else shouldCheckYCorner = true; }
        else if (x + radius >= worldHeight - 1 && isMovingDown && predictedY + radius > yBoundary) {
            final ITile downTile = tileArray[iy][ix + 1];
            if (!downTile.isPassable())
                predictedY = yBoundary - radius;
            else shouldCheckYCorner = true; }

        if (shouldCheckXCorner && shouldCheckYCorner)
        {
            final ITile cornerTile = tileArray[iy + ((isMovingDown) ? 1 : -1)][ix + ((isMovingRight) ? 1 : -1)];
            if (!cornerTile.isPassable())
                if (Math.abs(xSpeed) > Math.abs(ySpeed))
                    predictedY = y + ((isMovingDown) ? 1 : -1) * (float) Math.sqrt(radius * radius - (predictedX - x) * (predictedX - x));
                else
                    predictedX = x + ((isMovingRight) ? 1 : -1)* (float) Math.sqrt(radius * radius - (predictedY - y) * (predictedY - y));
        }

        actor.setCoordinates(new float[]{x + predictedX, y + predictedY});
        processedEventQueue.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, event.getEntityID(), predictedX, predictedY));
    }

    private void logGameCycleTime(long timeSpentWhileRunning) {
        if (timeSpentWhileRunning >= FIXED_TIME_STEP)
            LOGGER.warn("World " + this.toString() + " self updated. It took " + timeSpentWhileRunning + " >= " + FIXED_TIME_STEP + "! Fix the bugs!");
        else
            LOGGER.debug("World " + this.toString() + " self updated. It took " + timeSpentWhileRunning + " < " + FIXED_TIME_STEP + ". OK.");
    }

    private final Queue<WorldEvent> newEventQueue = new LinkedList<>();       // Here are new events are stashed
    private final Queue<WorldEvent> processedEventQueue = new LinkedList<>(); // State describer will take events from this list.

    private final AtomicInteger uidManager = new AtomicInteger(0);

    private final String name;

    private final ITile[][] tileArray;
    private final ArrayList<Bomberman> bombermen = new ArrayList<>(4);

    private final float[][] spawnLocations;
    private boolean areBombermenSpawned = false;
    private boolean areTilesPositioned = false;
    private boolean hasWorldReadyAcionFired = false;

    private final Runnable actionOnceWorldIsReady;

    private int selfUpdatingEntities = 0;
    public static final int FIXED_TIME_STEP = 25; //ms

    public static final float BOMB_RAY_HANDICAP_DIAMETER = 0.05f; // 0.75-0.05 will

    private static final Logger LOGGER = LogManager.getLogger(World.class);
}
