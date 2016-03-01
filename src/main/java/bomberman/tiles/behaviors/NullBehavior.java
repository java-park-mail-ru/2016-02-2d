package bomberman.tiles.behaviors;

import bomberman.interfaces.EventStashable;

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
