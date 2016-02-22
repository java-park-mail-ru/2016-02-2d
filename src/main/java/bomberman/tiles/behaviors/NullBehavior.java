package bomberman.tiles.behaviors;

import bomberman.Bomberman;
import bomberman.WorldEvent;
import bomberman.interfaces.EventStashable;
import bomberman.interfaces.EventType;
import bomberman.tiles.functors.ActionTileAbstractFunctor;

/**
 * Created by Frog on 22.02.2016.
 */
public class NullBehavior extends ActionTileAbstractBehavior {
    public NullBehavior(EventStashable eventList) {
        super(eventList);
    }

    @Override
    public void behave(float deltaTime) {
        // T_T
    }
}
