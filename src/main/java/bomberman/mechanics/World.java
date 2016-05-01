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
            final WorldEvent elderEvent = newEventQueue.poll();
            switch (elderEvent.getEventType()){
                case ENTITY_UPDATED:
                    processEntityUpdatedEvent(elderEvent);
                    break;
                case TILE_SPAWNED:
                    processTileSpawnedEvent(elderEvent);
                    break;
                case TILE_REMOVED:
                    processTileRemovedEvent(elderEvent);
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
        if (event.getEntityType() == EntityType.BOMB)
            explodeBomb(event);
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
        if (dx == 0 && dy == 0 || deltaT <= 0)
            return;

        final int worldWidth = tileArray.length;
        final int worldHeight = tileArray[0].length;

        final boolean isMovingRight = dx > 0;
        final boolean isMovingDown = dy > 0;

        boolean shouldCheckXCorner = false;
        boolean shouldCheckYCorner = false;

        final float x = actor.getCoordinates()[0];
        final int ix = (int) Math.floor(x);
        final float y = actor.getCoordinates()[1];
        final int iy = (int) Math.floor(y);

        final float xSpeed = actor.getMaximalSpeed() * (float) (dx / Math.sqrt(dx * dx + dy * dy));
        final float ySpeed = actor.getMaximalSpeed() * (float) (dy / Math.sqrt(dx * dx + dy * dy));

        float predictedX = x + xSpeed * (deltaT / 1000f);
        float predictedY = y + ySpeed * (deltaT / 1000f);
        final float radius = Bomberman.DIAMETER / 2;

        final float xBoundary = (float) (Math.floor(x) + ((isMovingRight) ? 1 : 0));
        final float yBoundary = (float) (Math.floor(y) + ((isMovingDown) ? 1 : 0));

        if (predictedX - radius < 0 && x < 1)        // If leaving world borders to left
            predictedX = radius;
        else if (predictedX + radius > worldWidth && x > worldWidth - 1)  // If leaving world borders to right
            predictedX = worldWidth - radius;
        else if (!isMovingRight && predictedX - radius < xBoundary) {    // If moving left and entering left tile
                final ITile leftTile = tileArray[iy][ix - 1];
                if (leftTile != null && !leftTile.isPassable())
                    predictedX = xBoundary + radius;        // If should collide, collide
                else shouldCheckXCorner = true; }           // else it is possible bomberman will collide to a corner.
        else if (isMovingRight && predictedX + radius > xBoundary) {
            final ITile rightTile = tileArray[iy][ix + 1];
            if (rightTile != null && !rightTile.isPassable())
                predictedX = xBoundary - radius;
            else shouldCheckXCorner = true; }

        if (predictedY - radius < 0 && y < 1)
            predictedY = radius;
        else if (predictedY + radius > worldHeight && y > worldHeight - 1)
            predictedY = worldHeight - radius;
        else if (!isMovingDown && predictedY - radius < yBoundary) {
            final ITile upTile = tileArray[iy - 1][ix];
            if (upTile != null && !upTile.isPassable())
                predictedY = yBoundary + radius;
            else shouldCheckYCorner = true; }
        else if (isMovingDown && predictedY + radius > yBoundary) {
            final ITile downTile = tileArray[iy + 1][ix];
            if (downTile != null && !downTile.isPassable())
                predictedY = yBoundary - radius;
            else shouldCheckYCorner = true; }

        if (shouldCheckXCorner && shouldCheckYCorner)
        {
            final ITile cornerTile = tileArray[iy + ((isMovingDown) ? 1 : -1)][ix + ((isMovingRight) ? 1 : -1)];
            if (cornerTile != null && !cornerTile.isPassable())
                if (Math.abs(xSpeed) > Math.abs(ySpeed))
                    predictedY = y + ((isMovingDown) ? 1 : -1) * (float) Math.sqrt(radius * radius - (predictedX - x) * (predictedX - x));
                else
                    predictedX = x + ((isMovingRight) ? 1 : -1)* (float) Math.sqrt(radius * radius - (predictedY - y) * (predictedY - y));
        }

        actor.setCoordinates(new float[]{ predictedX, predictedY});
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
            if (uniqueTile != null)
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
            selfUpdatingEntities++;
            processedEventQueue.add(new WorldEvent(EventType.TILE_SPAWNED, EntityType.BOMB, tileArray[y][x].getID(), x, y));
        }
    }

    private void explodeBomb(WorldEvent event) {
        final int x = (int) Math.floor(event.getX());
        final int y = (int) Math.floor(event.getY());

        final Ownable bomb = (Ownable) tileArray[y][x];
        final Bomberman owner = bomb.getOwner();
        final int radius = owner.getBombExplosionRange();

        owner.returnOnePlaceableBomb();
        selfUpdatingEntities--;

        for (int i = 0; i >= -radius; --i)
            if (x + i > 0)
                if (destroyTileAndSpawnRay(x + i, y, owner))
                    break;
        for (int i = 1; i <= radius; ++i)
            if (x + i < tileArray[0].length)
                if (destroyTileAndSpawnRay(x + i, y, owner))
                    break;

        for (int i = 0; i >= -radius; --i)
            if (y + i > 0)
                if (destroyTileAndSpawnRay(x, y + i, owner))
                    break;
        for (int i = 1; i <= radius; ++i)
            if (y + i < tileArray.length)
                if (destroyTileAndSpawnRay(x, y + i, owner))
                    break;
    }

    private boolean destroyTileAndSpawnRay(int x, int y, Bomberman owner) {
        if (tileArray[y][x] != null && !tileArray[y][x].isDestructible())   // break if undestuctible
            return true;
        boolean result = false;

        if (tileArray[y][x] != null && tileArray[y][x].isDestructible()) {
            newEventQueue.add(new WorldEvent(EventType.TILE_REMOVED, tileArray[y][x].getType(), tileArray[y][x].getID(), x, y));
            tileArray[y][x] = null;
            result = true;      // if destructible, destroy tile, spawn ray and break loop.
        }
        if (tileArray[y][x] == null) {
            tileArray[y][x] = TileFactory.getInstance().getNewTile(EntityType.BOMB_RAY, this, owner, getNextID());
            selfUpdatingEntities++;
            processedEventQueue.add(new WorldEvent(EventType.TILE_SPAWNED, EntityType.BOMB_RAY, tileArray[y][x].getID(), x, y));
        }
        return result;
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
