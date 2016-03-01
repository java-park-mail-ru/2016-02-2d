package bomberman.tiles.functors;

import bomberman.Bomberman;
import bomberman.WorldEvent;
import bomberman.interfaces.EventStashable;
import bomberman.interfaces.EventType;

public class DecreaseBombSpawnDelayFunctor extends ActionTileAbstractFunctor {

    public DecreaseBombSpawnDelayFunctor(EventStashable eventList) {
        super(eventList);
    }

    @Override
    public void applyAction(Bomberman bomberman) {
        bomberman.shortenBombSpawnTimer();
        eventList.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, bomberman.getType(), bomberman.getID()));
        eventList.addWorldEvent(new WorldEvent(EventType.TILE_REMOVED, owner.getType(), owner.getID()));
        owner.markForDestruction();
    }
}
