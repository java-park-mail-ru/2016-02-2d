package bomberman.service;

import bomberman.mechanics.World;
import bomberman.mechanics.WorldEvent;
import bomberman.mechanics.interfaces.EntityType;
import bomberman.mechanics.interfaces.EventType;
import bomberman.mechanics.worldbuilders.BasicWorldBuilder;
import bomberman.mechanics.worldbuilders.TextWorldBuilder;
import main.websockets.MessageSendable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import rest.UserProfile;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Room {

    public Room(){}

    public Room(int overrideCapacity) {
        capacity = overrideCapacity;
    }

    public void createNewWorld(String type)
    {
        world = new World(type, playerMap.size(), this::broadcastFreshEvents);
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
            socket.sendMessage(MessageCreator.createUserJoinedMessage(entry.getKey()));

        websocketMap.put(user, socket);
        readinessMap.put(user, new Pair<>(false, false));
        broadcast(MessageCreator.createUserJoinedMessage(user));
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

        if (hasEveryoneLoadedContent && isEveryoneReady)
            TimeHelper.executeAfter(TIME_TO_WAIT_AFTER_READY, ()->
                {if (hasEveryoneLoadedContent && isEveryoneReady) {
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
        return isActive;
    }

    public void scheduleBombermanMovement(UserProfile user, int dirX, int dirY) {
        if (isActive) {
            final int bombermanID = reversePlayerMap.get(user);

            scheduledMovements.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermanID, dirX, dirY, TimeHelper.now()));  // TODO: Should TimeHelper.now() be client's timestamp?

            updateIfNeeded();
        }
    }

    private void passScheduledMovementsToWorld() {
        for (WorldEvent movement: scheduledMovements)
            world.addWorldEvent(movement);
        scheduledMovements.clear();
    }

    private void transmitEventsOnWorldCreation() {
        isActive = true;
        broadcastFreshEvents();
    }

    private void broadcastFreshEvents() {
        for (WorldEvent event : world.getFreshEvents())
        switch (event.getEventType()) {
            case ENTITY_UPDATED:
                broadcast(MessageCreator.createObjectChangedMessage(event));
                break;
            case TILE_SPAWNED:
                broadcast(MessageCreator.createObjectSpawnedMessage(event));
                break;
            case TILE_REMOVED:
                broadcast(MessageCreator.createObjectDesrtoyedMessage(event));
                break;
        }
    }

    // http://gafferongames.com/game-physics/fix-your-timestep/
    // Variable delta time variant
    private void updateIfNeeded() {
        if (!updateScheduled) {
            updateScheduled = true;
            final long beforeUpdate = TimeHelper.now();
            passScheduledMovementsToWorld();

            world.runGameCycle(previousTickDuration);
            final long gameLoopTook = TimeHelper.now() - beforeUpdate;

            TimeHelper.sleepFor(MINIMAL_TIME_STEP - gameLoopTook);
            broadcastFreshEvents();

            previousTickDuration = beforeUpdate - TimeHelper.now();
            logGameCycleTime(previousTickDuration);

            updateScheduled = world.shouldBeUpdated();
        }
    }

    private void logGameCycleTime(long timeSpentWhileRunning) {
        if (timeSpentWhileRunning >= Room.MINIMAL_TIME_STEP)
            LOGGER.warn("Room " + this.toString() + " updated. It took " + timeSpentWhileRunning + " >= " + Room.MINIMAL_TIME_STEP + "! Fix the bugs!");
        else
            LOGGER.debug("Room " + this.toString() + " updated. It took " + timeSpentWhileRunning + " < " + Room.MINIMAL_TIME_STEP + ". OK.");
    }

    // I can't determine hashCode and equals methods. :(

    private int capacity = DEFAULT_CAPACITY;
    private boolean isEveryoneReady = false;
    private boolean hasEveryoneLoadedContent = false;

    private final Map<Integer, UserProfile> playerMap = new HashMap<>(4);
    private final Map<UserProfile, Integer> reversePlayerMap = new HashMap<>(4);
    private final Map<UserProfile, MessageSendable> websocketMap = new HashMap<>(4);
    private final Map<UserProfile, Pair<Boolean, Boolean>> readinessMap = new HashMap<>(4);

    World world;
    private boolean isActive = false;
    private boolean updateScheduled = false;
    private long previousTickDuration = MINIMAL_TIME_STEP;
    public static final int MINIMAL_TIME_STEP = 25; //ms

    private final Queue<WorldEvent> scheduledMovements = new ConcurrentLinkedQueue<>();

    public static final int DEFAULT_CAPACITY = 4;
    public static final int TIME_TO_WAIT_AFTER_READY = 3000; // ms

    private static final Logger LOGGER = LogManager.getLogger(Room.class);
}
