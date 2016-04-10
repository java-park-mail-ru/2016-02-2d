package bomberman.gameMechanics.tiles.functors;

import bomberman.gameMechanics.Bomberman;
import bomberman.gameMechanics.WorldEvent;
import bomberman.gameMechanics.interfaces.EventStashable;
import bomberman.gameMechanics.interfaces.EventType;

public class DecreaseBombExplosionDelayFunctor extends ActionTileAbstractFunctor {

    public DecreaseBombExplosionDelayFunctor(EventStashable eventList) {
        super(eventList);
    }

    @Override
    public void applyAction(Bomberman bomberman) {
        bomberman.shortenBombExplosionDelay();
        eventList.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, bomberman.getType(), bomberman.getID()));
        eventList.addWorldEvent(new WorldEvent(EventType.TILE_REMOVED, owner.getType(), owner.getID()));
        owner.markForDestruction();
    }
}
