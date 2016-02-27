package bomberman.tiles.behaviors;

import bomberman.WorldEvent;
import bomberman.interfaces.EventStashable;
import bomberman.interfaces.EventType;

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
