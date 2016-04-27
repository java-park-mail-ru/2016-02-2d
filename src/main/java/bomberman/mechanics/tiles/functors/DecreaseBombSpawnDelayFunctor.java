package bomberman.mechanics.tiles.functors;

import bomberman.mechanics.Bomberman;
import bomberman.mechanics.WorldEvent;
import bomberman.mechanics.interfaces.EventStashable;
import bomberman.mechanics.interfaces.EventType;

public class DecreaseBombSpawnDelayFunctor extends ActionTileAbstractFunctor {

    public DecreaseBombSpawnDelayFunctor(EventStashable eventList) {
        super(eventList);
    }

    @Override
    public void applyAction(Bomberman bomberman) {
        bomberman.shortenBombSpawnTimer();
        //eventList.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, bomberman.getType(), bomberman.getID(), 0, 0));
        eventList.addWorldEvent(new WorldEvent(EventType.TILE_REMOVED, owner.getType(), owner.getID(), 0, 0));
        owner.markForDestruction();
    }
}
