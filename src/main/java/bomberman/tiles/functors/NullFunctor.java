package bomberman.tiles.functors;

import bomberman.Bomberman;
import bomberman.interfaces.EventStashable;

public class NullFunctor extends ActionTileAbstractFunctor {
    public NullFunctor(EventStashable eventList) {
        super(eventList);
    }

    @Override
    public void applyAction(Bomberman bomberman) {
        // Nothing to do. Another design issue. Who could ever thought that bomb may not have any actions when bomberman steps on it? Even though it is unpassable...
    }
}
