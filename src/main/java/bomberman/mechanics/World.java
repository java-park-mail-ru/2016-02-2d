package bomberman.mechanics;

import bomberman.mechanics.interfaces.*;
import bomberman.mechanics.tiles.behaviors.BombRayBehavior;
import bomberman.mechanics.worldbuilders.WorldData;
import bomberman.service.TimeHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class World {

    public World(String worldType, int numberOfPlayers, Runnable actionOnWorldUpdated) {
        final IWorldBuilder builder = WorldBuilderForeman.getWorldBuilderInstance(worldType);
        final WorldData worldData = builder.getWorldData(this);

        actionOnUpdate = actionOnWorldUpdated;
        tileArray = worldData.getTileArray();
        spawnLocations = worldData.getSpawnList();
        name = worldData.getName();
        registerNewTiles();
    }

    public void addWorldEvent(WorldEvent worldEvent) {
        newEventQueue.add(worldEvent);
    }

    public int getNextID() {
        return uidManager.getAndIncrement();
    }

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

    public int getBombermanCount() {
        return bombermen.size();
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
        else if (event.getEntityType() == EntityType.BOMB_RAY)
            dissipateBombRay(event);
        else if (event.getEntityType() == EntityType.BOMBERMAN)
            killBomberman(event);
        else removeTileByID(event.getEntityID());
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

        for (Bomberman bomberman : bombermen) {
            Triplet<Float, Float, Long> previousMovement = bomberman.getMovementDirection();
            final boolean wasUpdating = bomberman.shouldBeUpdated();
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

            if (!wasUpdating && bomberman.shouldBeUpdated())
                selfUpdatingEntities++;
            if (wasUpdating && !bomberman.shouldBeUpdated())
                selfUpdatingEntities--;

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

        float predictedX = x + xSpeed * deltaT;
        float predictedY = y + ySpeed * deltaT;
        final float radius = Bomberman.DIAMETER / 2;

        final float xBoundary = (float) (Math.floor(x) + ((isMovingRight) ? 1 : 0));
        final float yBoundary = (float) (Math.floor(y) + ((isMovingDown) ? 1 : 0));

        for (Bomberman bomberman : bombermen)           // Low-quality inter-bomberman collision checker.
            if (bomberman.getID() != actor.getID()) {
                final float xDistance = Math.abs(x - bomberman.getCoordinates()[0]);
                final float yDistance = Math.abs(y - bomberman.getCoordinates()[1]);
                final float distance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);
                if (distance <= radius * 2)
                    return;
            }

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
            try {
                uniqueTiles.add(tileArray[uniqueCoordinate.getValue1()][uniqueCoordinate.getValue0()]);
            } catch (ArrayIndexOutOfBoundsException ex) {
                LOGGER.error("Error while activating tiles. Bomberman #" + actor.getID() + " on x: " + x + ", y: " + y + ". Error on tile x: " + uniqueCoordinate.getValue0() + ", y: " + uniqueCoordinate.getValue1() + '.', ex);
            }

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
            actor.resetBombTimer();
            selfUpdatingEntities++;
            processedEventQueue.add(new WorldEvent(EventType.TILE_SPAWNED, EntityType.BOMB, tileArray[y][x].getID(), x, y));
        }
    }

    @SuppressWarnings("OverlyComplexMethod")
    private void explodeBomb(WorldEvent event) {
        int x = -1;
        int y = -1;
        Ownable bomb = null;

        for (int j = 0; j < tileArray.length; ++j)
            for (int i = 0; i < tileArray[0].length; ++i)
                if (tileArray[j][i] != null && tileArray[j][i].getID() == event.getEntityID()) {
                    x = i;
                    y = j;
                    bomb = (Ownable) tileArray[y][x];
                }

        if (bomb == null) {
            LOGGER.error("Non-existent bomb #" + event.getEntityID() + " has exploded! O_o");
            return;
        }


        final Bomberman owner = bomb.getOwner();
        final int radius = owner.getBombExplosionRange();

        tileArray[y][x] = null;
        owner.returnOnePlaceableBomb();
        selfUpdatingEntities--;
        processedEventQueue.add(event);

        for (int i = 0; i >= -radius; --i)
            if (x + i >= 0)
                if (destroyTileAndSpawnRay(x + i, y, owner))
                    break;
        for (int i = 1; i <= radius; ++i)
            if (x + i < tileArray[0].length)
                if (destroyTileAndSpawnRay(x + i, y, owner))
                    break;

        for (int i = 0; i >= -radius; --i)
            if (y + i >= 0)
                if (destroyTileAndSpawnRay(x, y + i, owner))
                    break;
        for (int i = 1; i <= radius; ++i)
            if (y + i < tileArray.length)
                if (destroyTileAndSpawnRay(x, y + i, owner))
                    break;
    }

    private boolean destroyTileAndSpawnRay(int x, int y, Bomberman owner) {
        if (tileArray[y][x] != null && !tileArray[y][x].isDestructible())   // stop if undestuctible
            return true;
        boolean result = false;

        if (tileArray[y][x] != null && tileArray[y][x].isDestructible()) {
            newEventQueue.add(new WorldEvent(EventType.TILE_REMOVED, tileArray[y][x].getType(), tileArray[y][x].getID(), x, y));

            tileArray[y][x] = null;
            result = true;      // if destructible, destroy tile, spawn ray and break loop.
            if (new Random(new Date().hashCode()).nextInt() % 100 + 1 < PERCENT_TO_SPAWN_BONUS)
                TimeHelper.executeAfter((int) (BombRayBehavior.BOMB_RAY_DURATION * MILLISECONDS_IN_SECOND) + 100, () -> spawnrandomBonus(x, y));
        }
        if (tileArray[y][x] == null) {
            tileArray[y][x] = TileFactory.getInstance().getNewTile(EntityType.BOMB_RAY, this, owner, getNextID());
            selfUpdatingEntities++;
            processedEventQueue.add(new WorldEvent(EventType.TILE_SPAWNED, EntityType.BOMB_RAY, tileArray[y][x].getID(), x, y));
        }
        return result;
    }

    private void dissipateBombRay(WorldEvent event) {
        final int x = (int) Math.floor(event.getX());
        final int y = (int) Math.floor(event.getY());

        if (tileArray[y][x] != null && tileArray[y][x].getID() == event.getEntityID())     // Workaround for one bomb_ray exploding over second one.
            tileArray[y][x] = null;

        selfUpdatingEntities--;

        processedEventQueue.add(event);
    }

    private void killBomberman(WorldEvent event) {
        bombermen.remove(getBombermanByID(event.getEntityID()));
        processedEventQueue.add(event);
    }

    private void spawnrandomBonus(int x, int y) {
        final EntityType type;
        switch (new Random(new Date().hashCode()).nextInt() % TileFactory.getBonusCount()) {
            case 0:
                type = EntityType.BONUS_INCMAXRANGE;
                break;
            case 1:
                type = EntityType.BONUS_DECBOMBSPAWN;
                break;
            case 2:
                type = EntityType.BONUS_DECBOMBFUSE;
                break;
            case 3:
                type = EntityType.BONUS_INCMAXHP;
                break;
            case 4:
                type = EntityType.BONUS_INCSPEED;
                break;
            case 5:
                type = EntityType.BONUS_HEAL;
                break;
            case 6:
                type = EntityType.BONUS_MOREBOMBS;
                break;
            default:
                return;
        }

        final ITile bonusTile = TileFactory.getInstance().getNewTile(type, this, getNextID());
        tileArray[y][x] = bonusTile;
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

    private void removeTileByID(int id) {
        for (int y = 0; y < tileArray.length; ++y)
            for (int x = 0; x < tileArray[0].length; ++x)
                if (tileArray[y][x] != null && tileArray[y][x].getID() == id) {
                    processedEventQueue.add(new WorldEvent(EventType.TILE_REMOVED, tileArray[y][x].getType(), tileArray[y][x].getID(), x, y));
                    tileArray[y][x] = null;
                }
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

    private int selfUpdatingEntities = 1;

    public static final float ACTION_TILE_HANDICAP_DIAMETER = 0.05f; // 0.75-0.05 will
    public static final int PERCENT_TO_SPAWN_BONUS = 33;
    private static final float MILLISECONDS_IN_SECOND = 1000f;

    private static final Logger LOGGER = LogManager.getLogger(World.class);
}
