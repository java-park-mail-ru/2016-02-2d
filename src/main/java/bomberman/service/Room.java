package bomberman.service;

import bomberman.mechanics.World;
import bomberman.mechanics.WorldEvent;
import bomberman.mechanics.interfaces.EntityType;
import bomberman.mechanics.interfaces.EventType;
import main.websockets.MessageSendable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import rest.UserProfile;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Room {

    public Room(){}

    public Room(int overrideCapacity) {
        capacity = overrideCapacity;
    }

    public void createNewWorld(String type)
    {
        world = new World(type, playerMap.size(), this::broadcastFreshEvents);
        worldSpawnDetails = new LinkedList<>(world.getFreshEvents());
    }

    public void assignBombermenToPlayers() {
        world.spawnBombermen(websocketMap.size());
        final int[] bombermen = world.getBombermenIDs();
        int i = 0;
        for (Map.Entry<UserProfile, MessageSendable> player : websocketMap.entrySet()) {
            playerMap.put(bombermen[i], player.getKey());
            reversePlayerMap.put(player.getKey(), bombermen[i]);
            ++i;
        }
    }

    public int getMaximalCapacity() {
        return capacity;
    }

    public int getCurrentCapacity() {
        return websocketMap.size();
    }

    public boolean isFilled()
    {
        return websocketMap.size() >= capacity;
    }

    public boolean isEmpty() {
        return websocketMap.isEmpty();
    }

    public void insertPlayer(UserProfile user, MessageSendable socket) {
        isEveryoneReady = false;
        hasEveryoneLoadedContent = false;

        for (Map.Entry<UserProfile, MessageSendable> entry : websocketMap.entrySet())
            socket.sendMessage(MessageCreator.createUserJoinedMessage(entry.getKey(), readinessMap.get(entry.getKey()).getValue0(), readinessMap.get(entry.getKey()).getValue0()));

        for (WorldEvent spawnEvent: worldSpawnDetails)
            transmit(spawnEvent, socket);

        websocketMap.put(user, socket);
        readinessMap.put(user, new Pair<>(false, false));
        broadcast(MessageCreator.createUserJoinedMessage(user, readinessMap.get(user).getValue0(), readinessMap.get(user).getValue1()));
    }

    public boolean hasPlayer(UserProfile user) {
        return websocketMap.containsKey(user);
    }

    public void removePlayer(UserProfile user) {
        if (websocketMap.containsKey(user)) {
            websocketMap.remove(user);
            readinessMap.remove(user);
            broadcast(MessageCreator.createUserLeftMessage(user));
        }
    }

    public void updatePlayerState(UserProfile user, boolean isReady, boolean contentLoaded) {
        readinessMap.remove(user);
        readinessMap.put(user, new Pair<>(isReady, contentLoaded));
        broadcast(MessageCreator.createUserStateChangedMessage(user, isReady, contentLoaded));

        boolean isEveryoneReadyTMP = true;
        boolean hasEveryoneLoadedContentTMP = true;
        for (Map.Entry<UserProfile, Pair<Boolean, Boolean>> entry : readinessMap.entrySet())
        {
            if (!entry.getValue().getValue0())
                isEveryoneReadyTMP = false;
            if (!entry.getValue().getValue1())
                hasEveryoneLoadedContentTMP = false;
        }
        isEveryoneReady = isEveryoneReadyTMP;
        hasEveryoneLoadedContent = hasEveryoneLoadedContentTMP;

        if (websocketMap.size() > 1 && hasEveryoneLoadedContent && isEveryoneReady)
            TimeHelper.executeAfter(TIME_TO_WAIT_AFTER_READY, ()->
                {if (websocketMap.size() > 1 && hasEveryoneLoadedContent && isEveryoneReady) {
                    assignBombermenToPlayers();
                    transmitEventsOnWorldCreation();
                    broadcast(MessageCreator.createWorldCreatedMessage(world.getName(), world.getWidth(), world.getHeight()));
                }
            });
            // else break;
    }

    public void broadcast(String message) {
        for (Map.Entry<UserProfile, MessageSendable> entry: websocketMap.entrySet())
            entry.getValue().sendMessage(message);
    }

    public boolean isActive() {
        return isActive.get();
    }

    public void scheduleBombermanMovement(UserProfile user, int dirX, int dirY) {
        if (isActive.get() && !isFinished.get()) {
            final int bombermanID = reversePlayerMap.get(user);

            scheduledMovements.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermanID, dirX, dirY, TimeHelper.now()));  // TODO: Should TimeHelper.now() be client's timestamp?

            updateIfNeeded();
        }
    }

    public void scheduleBombPlacement(UserProfile user) {
        if (isActive.get() && !isFinished.get()) {
            final int bombermanID = reversePlayerMap.get(user);

            scheduledMovements.add(new WorldEvent(EventType.TILE_SPAWNED, EntityType.BOMB, bombermanID, 0, 0));

            updateIfNeeded();
        }
    }

    private void passScheduledMovementsToWorld() {
        WorldEvent event = scheduledMovements.poll();
        while (event != null) {
            world.addWorldEvent(event);
            event = scheduledMovements.poll();
        }
    }

    private void transmit(WorldEvent event, MessageSendable socket) {
        switch (event.getEventType()) {
            case ENTITY_UPDATED:
                socket.sendMessage(MessageCreator.createObjectChangedMessage(event));
                break;
            case TILE_SPAWNED:
                socket.sendMessage(MessageCreator.createObjectSpawnedMessage(event));
                break;
            case TILE_REMOVED:
                socket.sendMessage(MessageCreator.createObjectDestroyedMessage(event));
                break;
        }
    }

    private void transmitEventsOnWorldCreation() {
        isActive.set(true);
        broadcastFreshEvents();
    }

    private void broadcastFreshEvents() {
        for (WorldEvent event : world.getFreshEvents())
            switch (event.getEventType()) {
                case ENTITY_UPDATED:
                    broadcast(MessageCreator.createObjectChangedMessage(event));
                    break;
                case TILE_SPAWNED:
                    if (event.getEntityType() == EntityType.BOMBERMAN)
                        broadcast(MessageCreator.createBombermanSpawnedMessage(event, (int) playerMap.get(event.getEntityID()).getId()));
                    else
                        broadcast(MessageCreator.createObjectSpawnedMessage(event));
                    break;
                case TILE_REMOVED:
                    broadcast(MessageCreator.createObjectDestroyedMessage(event));
                    break;
            }
    }

    // http://gafferongames.com/game-physics/fix-your-timestep/
    // Variable delta time variant
    private void updateIfNeeded() {
        if (!updateScheduled.get() && !isFinished.get()) {
            updateScheduled.set(true);
            update();
        }
    }

    private void update() {
        final long beforeUpdate = TimeHelper.now();
        passScheduledMovementsToWorld();

        world.runGameLoop(previousTickDuration);
        final long gameLoopTook = TimeHelper.now() - beforeUpdate;

        TimeHelper.sleepFor(MINIMAL_TIME_STEP - gameLoopTook);
        broadcastFreshEvents();

        previousTickDuration = TimeHelper.now() - beforeUpdate;
        logGameCycleTime(gameLoopTook);

        updateScheduled.set(world.shouldBeUpdated());

        stopIfGameIsOver();

        if (updateScheduled.get() && !isFinished.get())
            update();
    }

    private void logGameCycleTime(long timeSpentWhileRunning) {
        if (timeSpentWhileRunning >= Room.MINIMAL_TIME_STEP)
            LOGGER.warn("Room " + this.toString() + " updated. It took " + timeSpentWhileRunning + " >= " + Room.MINIMAL_TIME_STEP + "! Fix the bugs!");
        else
            LOGGER.debug("Room " + this.toString() + " updated. It took " + timeSpentWhileRunning + " < " + Room.MINIMAL_TIME_STEP + ". OK.");
    }

    private void stopIfGameIsOver() {
        if (world.getBombermanCount() == 1) {
            TimeHelper.executeAfter(TIME_TO_WAIT_ON_GAME_OVER, () -> {
                if (!isFinished.get() && world.getBombermanCount() == 1) {
                    isFinished.set(true);
                    broadcast(MessageCreator.createGameOverMessage(playerMap.get(world.getBombermenIDs()[0])));
                }
            });
        }
        if (world.getBombermanCount() == 0) {
            isFinished.set(true);
            broadcast(MessageCreator.createGameOverMessage(null));
        }
    }

    // I can't determine hashCode and equals methods. :(

    private int capacity = DEFAULT_CAPACITY;
    private volatile boolean isEveryoneReady = false;
    private volatile boolean hasEveryoneLoadedContent = false;

    private final Map<Integer, UserProfile> playerMap = new HashMap<>(4);
    private final Map<UserProfile, Integer> reversePlayerMap = new HashMap<>(4);
    private final Map<UserProfile, MessageSendable> websocketMap = new HashMap<>(4);
    private final Map<UserProfile, Pair<Boolean, Boolean>> readinessMap = new HashMap<>(4);

    World world;
    private AtomicBoolean isActive = new AtomicBoolean(false);
    private AtomicBoolean isFinished = new AtomicBoolean(false);
    private AtomicBoolean updateScheduled = new AtomicBoolean(false);
    private long previousTickDuration = MINIMAL_TIME_STEP;
    public static final int MINIMAL_TIME_STEP = 25; //ms

    private final Queue<WorldEvent> scheduledMovements = new ConcurrentLinkedQueue<>();
    private List<WorldEvent> worldSpawnDetails = new LinkedList<>();       // worldHistory, heh?

    public static final int DEFAULT_CAPACITY = 4;
    public static final int TIME_TO_WAIT_AFTER_READY = 3000; // ms
    public static final int TIME_TO_WAIT_ON_GAME_OVER = 500; // ms

    private static final Logger LOGGER = LogManager.getLogger(Room.class);
}
