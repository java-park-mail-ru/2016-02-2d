package bomberman.mechanics.tiles.functors;

import bomberman.mechanics.Bomberman;
import bomberman.mechanics.World;
import bomberman.mechanics.interfaces.EventStashable;

public class BombRayFunctor extends ActionTileAbstractFunctor {

    public BombRayFunctor(World eventList) {
        super(eventList);
    }

    @Override
    public void applyAction(Bomberman bomberman) {
        bomberman.affectHealth(Bomberman.MAX_HEALTH_BASE_VALUE);
    }
}
