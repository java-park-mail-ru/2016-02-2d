package bomberman.mechanics.tiles.behaviors;

import bomberman.mechanics.WorldEvent;
import bomberman.mechanics.interfaces.EventStashable;
import bomberman.mechanics.interfaces.EventType;

public class BombBehavior extends ActionTileAbstractBehavior {
    public BombBehavior(EventStashable eventList, float timer) {
        super(eventList);
        bombTimer = timer;
        hasExploded = false;
    }

    @Override
    public void behave(float deltaTime) {
        bombTimer -= deltaTime;
        if (bombTimer <= 0 && !hasExploded)
        {
            eventList.addWorldEvent(new WorldEvent(EventType.TILE_REMOVED, owner.getType(), owner.getID()));
            hasExploded = true;
        }
    }

    private boolean hasExploded;
    private float bombTimer;
}
