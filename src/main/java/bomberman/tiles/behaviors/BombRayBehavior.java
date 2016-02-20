package bomberman.tiles.behaviors;

import bomberman.WorldEvent;
import bomberman.interfaces.EventStashable;
import bomberman.interfaces.EventType;
import bomberman.tiles.ActionTile;

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
            eventList.addWorldEvent(new WorldEvent(EventType.TILE_REMOVED, owner.getType(), owner.getID()));
            hasDissipated = true;
        }
    }

    private float bombTimer;
    private boolean hasDissipated;

    public final float BOMB_RAY_DURATION = 1.0f;
}
