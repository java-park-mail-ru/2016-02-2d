package bomberman;

import bomberman.Interfaces.EntityType;
import bomberman.Interfaces.IEntity;

public class Bomberman implements IEntity {
    public Bomberman(int id) {
        super();
        this.id = id;
        bombSpawnTimerInitValue = BOMB_SPAWN_TIMER_BASE_VALUE;
    }

    @Override
    public int hashCode() {
        return getID();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
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


    public int getHealth() {
        return health;
    }

    private void shortenBombSpawnTimer()
    {
         bombSpawnTimerInitValue *= BOMB_SPAWN_TIMER_MULTIPLIER;
    }

    private int health;

    private float x;        // I know they're short, but I don't think that 'x' may mean something else than "xCoordinate"
    private float y;        // "yCoordinate" -> 'y'

    private int id;         // "uniqueIdentificationNumber" -> "id"

    private float bombSpawnTimer;
    private float bombSpawnTimerInitValue;

    public static final float BOMB_SPAWN_TIMER_BASE_VALUE = 2.5f; // 2.5 seconds
    public static final float BOMB_SPAWN_TIMER_MULTIPLIER = 0.8f; // Will be reduced by 1/5 of current value evry time. i.e 2.5->2->1.6->1.28

}
