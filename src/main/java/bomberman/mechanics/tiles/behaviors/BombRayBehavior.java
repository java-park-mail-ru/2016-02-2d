package bomberman.mechanics.tiles.behaviors;

import bomberman.mechanics.WorldEvent;
import bomberman.mechanics.interfaces.EventStashable;
import bomberman.mechanics.interfaces.EventType;

public class BombRayBehavior extends ActionTileAbstractBehavior {
    public BombRayBehavior(EventStashable eventList) {
        super(eventList);
        bombTimer = BOMB_RAY_DURATION;
        hasDissipated = false;
    }

    @Override
    public void behave(float deltaTime) {
        bombTimer -= deltaTime;
        if (bombTimer <= 0 && !hasDissipated)
        {
            eventList.addWorldEvent(new WorldEvent(EventType.TILE_REMOVED, owner.getType(), owner.getID(), 0, 0));
            hasDissipated = true;
        }
    }

    private float bombTimer;
    private boolean hasDissipated;

    public static final float BOMB_RAY_DURATION = 1.0f;
}
