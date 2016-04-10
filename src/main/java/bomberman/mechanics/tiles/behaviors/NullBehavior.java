package bomberman.mechanics.tiles.behaviors;

import bomberman.mechanics.interfaces.EventStashable;

public class NullBehavior extends ActionTileAbstractBehavior {
    public NullBehavior(EventStashable eventList) {
        super(eventList);
    }

    @Override
    public void behave(float deltaTime) {
        // T_T
    }
}
