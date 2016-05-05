package bomberman.mechanics.tiles.functors;

import bomberman.mechanics.Bomberman;
import bomberman.mechanics.World;
import bomberman.mechanics.tiles.functors.ActionTileAbstractFunctor;

public class NullFunctor extends ActionTileAbstractFunctor {
    public NullFunctor(World eventList) {
        super(eventList);
    }

    @Override
    public void applyAction(Bomberman bomberman) {
        // Nothing to do. Another design issue. Who could ever thought that bomb may not have any actions when bomberman steps on it? Even though it is unpassable...
    }
}
