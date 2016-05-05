package bomberman.mechanics;

import bomberman.mechanics.interfaces.EntityType;
import bomberman.mechanics.interfaces.EventType;
import bomberman.mechanics.interfaces.IEntity;
import bomberman.service.TimeHelper;
import org.javatuples.Triplet;

import java.util.LinkedList;
import java.util.Queue;

public class Bomberman implements IEntity {

    public Bomberman(int id, World world) {
        this.id = id;
        bombSpawnTimerInitValue = BOMB_SPAWN_TIMER_BASE_VALUE;
        bombExplosionRange = BOMB_BASE_RANGE;
        maxHealth = MAX_HEALTH_BASE_VALUE;
        health = maxHealth;
        bombExplosionDelay = BOMB_BASE_EXPLOSION_DELAY;
        maxBombsCanBePlaced = BASE_BOMB_AMOUNT;
        currentPlaceableBombs = maxBombsCanBePlaced;
        maximalSpeed = BASE_MAX_SPEED;
        this.world = world;
    }

    @Override
    public float[] getCoordinates() {
        return new float[]{x, y};
    }

    @Override
    public void setCoordinates(float[] coords) {
        x = coords[0];
        y = coords[1];
    }

    @Override
    public EntityType getType() {
        return EntityType.BOMBERMAN;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void update(float deltaTime) {
        if (bombSpawnTimer >= 0)
            bombSpawnTimer =- deltaTime;
        // else it can be somewhere below zero. xD (if dT is about 1000 ms)
    }

    public boolean canSpawnBomb() {
        return bombSpawnTimer <= 0 && currentPlaceableBombs >= 0;
    }


    //
    // Health Actions
    //

    // Use negative amounts for healing! =D
    @Override
    public void affectHealth(int amount) {
        if (health > maxHealth)
            health = maxHealth;

        health -= amount;

        if (health < 0)
            world.addWorldEvent(new WorldEvent(EventType.TILE_REMOVED, EntityType.BOMBERMAN, id, x, y, TimeHelper.now()));
    }

    public void increaseMaxHealth() {
        maxHealth += MAX_HEALTH_INCREMENT;
        health += MAX_HEALTH_INCREMENT;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }


    //
    // Bomb Range Actions
    //

    public int getBombExplosionRange() {
        return bombExplosionRange;
    }

    public void increaseExplosionRange()
    {
        bombExplosionRange += BOMB_RANGE_INCREMENT;
    }


    //
    // Bomb SpawnTimer Actions
    //

    public void resetBombTimer()
    {
        bombSpawnTimer = bombSpawnTimerInitValue;
    }

    public void shortenBombSpawnTimer()
    {
         bombSpawnTimerInitValue *= BOMB_SPAWN_TIMER_MULTIPLIER;
    }


    //
    // Bomb Explosion Delay Actions
    //

    public void shortenBombExplosionDelay() {
        bombExplosionDelay *= BOMB_EXPLOSION_DELAY_MULTIPLIER;
    }

    public float getBombExplosionDelay() {
        return bombExplosionDelay;
    }


    //
    // Bomb Placement Actions
    //

    public int getCurrentPlaceableBombAmount() {
        return currentPlaceableBombs;
    }

    public void takeOnePlaceableBomb() {
        currentPlaceableBombs--;
    }

    public void returnOnePlaceableBomb() {
        currentPlaceableBombs++;
    }

    public void increaseMaxPlaceableBombs() {
        maxBombsCanBePlaced += BOMB_AMOUNT_INCREMENT;
        currentPlaceableBombs += BOMB_AMOUNT_INCREMENT;
    }

    //
    // Speed Actions
    //

    public void increaseMaximalSpeed() {
        maximalSpeed += MAX_SPEED_INCREMENT;
    }

    public float getMaximalSpeed() {
        return maximalSpeed;
    }

    public Triplet<Float, Float, Long> getMovementDirection() {
        return movementDirection;
    }

    public void setMovementDirection(Triplet<Float, Float, Long> movementDirectionChange) {
        movementDirection = movementDirectionChange;
    }

    public Queue<Triplet<Float, Float, Long>> getMovementsDuringTick() {
        return movementsDuringTick;
    }

    public void addMovement(Triplet<Float, Float, Long> movementDirectionChange) {
        movementsDuringTick.add(movementDirectionChange);
    }

    // In-World desctription
    @SuppressWarnings("InstanceVariableNamingConvention")
    private float x;                // I know they're short, but I don't think that 'x' may mean something else than "xCoordinate"
    @SuppressWarnings("InstanceVariableNamingConvention")
    private float y;                // "yCoordinate" -> 'y'
    private final int id;           // "uniqueIdentificationNumber" -> "id"
    World world;


    // Health Description
    private int health;
    private int maxHealth;
    public static final int MAX_HEALTH_BASE_VALUE = 100;
    public static final int MAX_HEALTH_INCREMENT = 50;   // One powerup for double life. Three powerups for triple life. ()


    // Bomb Description
    private float bombSpawnTimer;
    private float bombSpawnTimerInitValue;
    public static final float BOMB_SPAWN_TIMER_BASE_VALUE = 2.5f; // 2.5 seconds
    public static final float BOMB_SPAWN_TIMER_MULTIPLIER = 0.8f; // Will be reduced by 1/5 of current value evry time. i.e 2.5->2->1.6->1.28

    private int bombExplosionRange;
    public static final int BOMB_BASE_RANGE = 1;    // 1 tile
    public static final int BOMB_RANGE_INCREMENT = 1;

    private float bombExplosionDelay;
    public static final float BOMB_BASE_EXPLOSION_DELAY = 2.0f;    // 2 seconds
    public static final float BOMB_EXPLOSION_DELAY_MULTIPLIER = 0.8f;

    private int currentPlaceableBombs;
    private int maxBombsCanBePlaced;
    public static final int BASE_BOMB_AMOUNT = 1;    // 1 tile
    public static final int BOMB_AMOUNT_INCREMENT = 1;

    private float maximalSpeed;
    public static final float BASE_MAX_SPEED = 0.5f;    // 0.5 tile per second
    public static final float MAX_SPEED_INCREMENT = 0.25f; // 0.5 → 0.75 → 1.0 → 1.25 → 1.5. Higher the harder. :)
    private Triplet<Float, Float, Long> movementDirection = new Triplet<>(0f, 0f, 0L);
    private Queue<Triplet<Float, Float, Long>> movementsDuringTick = new LinkedList<>();

    public static final float DIAMETER = 0.75f; // ¾ of a tile.
}
