package bomberman;

import bomberman.interfaces.EntityType;
import bomberman.interfaces.IEntity;

public class Bomberman implements IEntity {

    public Bomberman(int id) {
        super();
        this.id = id;
        bombSpawnTimerInitValue = BOMB_SPAWN_TIMER_BASE_VALUE;
        bombExplosionRange = BOMB_BASE_RANGE;
        maxHealth = MAX_HEALTH_BASE_VALUE;
        health = maxHealth;
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

    // Use negative amounts for healing! =D
    @Override
    public void affectHealth(int amount) {
        health -= amount;
        if (health > maxHealth) health = maxHealth;
    }

    public void increaseMaxHealth() {
        maxHealth += MAX_HEALTH_INCREMENT;
        health += MAX_HEALTH_INCREMENT;
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
        // TODO: some logic. Movement, bombtimer, etc.
        if (bombSpawnTimer >= 0)
            bombSpawnTimer =- deltaTime;

    }

    public boolean canSpawnBomb() {
        if (bombSpawnTimer <= 0) {
            bombSpawnTimer = bombSpawnTimerInitValue;
            return true;
        }
        return false;
    }

    public int getBombExplosionRange() {
        return bombExplosionRange;
    }

    public void increaseExplosionRange()
    {
        bombExplosionRange += BOMB_RANGE_INCREMENT;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    private void shortenBombSpawnTimer()
    {
         bombSpawnTimerInitValue *= BOMB_SPAWN_TIMER_MULTIPLIER;
    }

    // In-World desctription
    private float x;        // I know they're short, but I don't think that 'x' may mean something else than "xCoordinate"
    private float y;        // "yCoordinate" -> 'y'
    private int id;         // "uniqueIdentificationNumber" -> "id"

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



}
