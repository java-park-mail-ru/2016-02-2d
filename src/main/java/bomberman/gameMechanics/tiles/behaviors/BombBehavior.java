package bomberman.gameMechanics.tiles.behaviors;

import bomberman.gameMechanics.WorldEvent;
import bomberman.gameMechanics.interfaces.EventStashable;
import bomberman.gameMechanics.interfaces.EventType;

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
