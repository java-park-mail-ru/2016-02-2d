package bomberman.mechanics;

import bomberman.mechanics.interfaces.*;
import bomberman.service.Room;
import bomberman.service.TimeHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class World implements EventStashable, UniqueIDManager, EventObtainable {

    public World(String worldType, int numberOfPlayers, Runnable actionOnWorldUpdated) {
        final IWorldBuilder builder = WorldBuilderForeman.getWorldBuilderInstance(worldType);
        final Triplet<ITile[][], float[][], String> worldData = builder.getWorldData(this, this);

        actionOnUpdate = actionOnWorldUpdated;
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
            final Bomberman newBomberman = new Bomberman(getNextID(), this);
            newBomberman.setCoordinates(spawnLocations[i]);
            bombermen.add(newBomberman);
            processedEventQueue.add(new WorldEvent(EventType.TILE_SPAWNED, newBomberman.getType(), newBomberman.getID(), spawnLocations[i][0], spawnLocations[i][1]));
        }
        areBombermenSpawned = true;
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return tileArray[0].length;
    }

    public int getHeight() {
        return tileArray.length;
    }

    public boolean shouldBeUpdated() {
        return selfUpdatingEntities > 0 || !newEventQueue.isEmpty();
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

    public void runGameLoop(long deltaT) {
        updateEverything(deltaT);

        while (!newEventQueue.isEmpty())
        {
            final WorldEvent elderEvent = newEventQueue.remove();
            switch (elderEvent.getEventType()){
                case ENTITY_UPDATED:
                    processEntityUpdatedEvent(elderEvent);
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

        tryMovingBombermen(deltaT);
    }

    private void processEntityUpdatedEvent(WorldEvent event) {
        LOGGER.debug("Processing object_updated");
        assignBombermanMovement(event);
    }

    private void processTileSpawnedEvent(WorldEvent event) {
        LOGGER.debug("Processing object_spawned");
        tryPlacingBomb(event.getEntityID());
    }

    private void processTileRemovedEvent(WorldEvent event) {
        LOGGER.debug("Processing object_destroyed");
        // TODO: Bomb explosion
        // TODO: Bomb Ray dissipation
        // TODO: Bonuses pickup?
    }

    private void assignBombermanMovement(WorldEvent event) {
        final Bomberman actor = getBombermanByID(event.getEntityID());

        if (actor == null) {
            LOGGER.warn("No bomberman with id \"" + event.getEntityID() + "\" exists!");
            return;
        }

        actor.addMovement(new Triplet<>(event.getX(), event.getY(), event.getTimestamp()));
    }

    // Movements are not at the same time, they have bomberman spawn priorities over timestap priorities! Bad, yet should work on small deltaT intervals.
    private void tryMovingBombermen(long deltaT) {
        for (Bomberman bomberman: bombermen) {
            Triplet<Float, Float, Long> previousMovement = bomberman.getMovementDirection();
            long timeSpentMoving = 0;
            while (bomberman.getMovementsDuringTick().peek() != null) {
                final Triplet<Float, Float, Long> movement = bomberman.getMovementsDuringTick().poll();

                long dt = movement.getValue2() - previousMovement.getValue2();
                if (dt > deltaT)
                    dt = deltaT;

                timeSpentMoving += dt;

                tryMovingBomberman(bomberman, movement.getValue0(), movement.getValue1(), dt);
                activateTilesWereSteppedOn(bomberman);

                previousMovement = movement;
            }

            bomberman.setMovementDirection(previousMovement);

            tryMovingBomberman(bomberman, previousMovement.getValue0(), previousMovement.getValue1(), deltaT - timeSpentMoving);
            activateTilesWereSteppedOn(bomberman);
        }
    }

    // Hard maths. PM @Xobotun to get an explanation, don't be shy!
    @SuppressWarnings("OverlyComplexMethod")
    private void tryMovingBomberman(Bomberman actor, float dx, float dy, long deltaT) {
        final int worldWidth = tileArray.length - 1;
        final int worldHeight = tileArray[0].length - 1;

        final boolean isMovingRight = dx > 0;
        final boolean isMovingDown = dy > 0;

        boolean shouldCheckXCorner = false;
        boolean shouldCheckYCorner = false;

        final float x = actor.getCoordinates()[0];
        final int ix = (int) Math.floor(x);
        final float y = actor.getCoordinates()[1];
        final int iy = (int) Math.floor(y);

        final float xSpeed = ((isMovingRight) ? 1 : -1) * actor.getMaximalSpeed() * (float)(dx / Math.sqrt(dx * dx + dy * dy));
        final float ySpeed = ((isMovingDown) ? 1 : -1) * actor.getMaximalSpeed() * (float) (dy / Math.sqrt(dx * dx + dy * dy));

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
        processedEventQueue.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, actor.getID(), predictedX, predictedY));
    }

    private void activateTilesWereSteppedOn(Bomberman actor) {
        final float x = actor.getCoordinates()[0];
        final float y = actor.getCoordinates()[1];

        final float handicappedRadius = (Bomberman.DIAMETER - ACTION_TILE_HANDICAP_DIAMETER) / 2;

        final Set<Pair<Integer, Integer>> uniqueTileCoordinates = new HashSet<>(4);

        uniqueTileCoordinates.add(new Pair<>((int) Math.floor(x - handicappedRadius), (int) Math.floor(y - handicappedRadius)));
        uniqueTileCoordinates.add(new Pair<>((int) Math.floor(x - handicappedRadius), (int) Math.floor(y + handicappedRadius)));
        uniqueTileCoordinates.add(new Pair<>((int) Math.floor(x + handicappedRadius), (int) Math.floor(y - handicappedRadius)));
        uniqueTileCoordinates.add(new Pair<>((int) Math.floor(x + handicappedRadius), (int) Math.floor(y + handicappedRadius)));

        final Set<ITile> uniqueTiles = new HashSet<>(4);
        for (Pair<Integer, Integer> uniqueCoordinate: uniqueTileCoordinates)
            uniqueTiles.add(tileArray[uniqueCoordinate.getValue1()][uniqueCoordinate.getValue0()]);

        for (ITile uniqueTile: uniqueTiles)
            uniqueTile.applyAction(actor);
    }

    private void tryPlacingBomb(int bombermanID) {
        final Bomberman actor = getBombermanByID(bombermanID);
        if (actor == null)
            return;

        final int x = (int) Math.floor(actor.getCoordinates()[0]);
        final int y = (int) Math.floor(actor.getCoordinates()[1]);

        if (actor.canSpawnBomb() && tileArray[y][x] == null)
        {
            tileArray[y][x] = TileFactory.getInstance().getNewTile(EntityType.BOMB, this, actor, getNextID());
            actor.takeOnePlaceableBomb();
            processedEventQueue.add(new WorldEvent(EventType.TILE_SPAWNED, EntityType.BOMB, tileArray[y][x].getID(), x, y));
        }
    }

    @Nullable
    private Bomberman getBombermanByID(int id) {
        Bomberman result = null;

        for (Bomberman bomberman : bombermen)
            if (bomberman.getID() == id)
                result = bomberman;

        return result;
    }

    private void updateEverything(long deltaT) {
        for (Bomberman bomberman : bombermen)
            bomberman.update(deltaT);

        for (ITile[] row: tileArray)
            for (ITile tile: row)
                if (tile != null)
                    tile.update(deltaT);
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

    private final Runnable actionOnUpdate;

    private int selfUpdatingEntities = 0;

    public static final float ACTION_TILE_HANDICAP_DIAMETER = 0.05f; // 0.75-0.05 will

    private static final Logger LOGGER = LogManager.getLogger(World.class);
}
