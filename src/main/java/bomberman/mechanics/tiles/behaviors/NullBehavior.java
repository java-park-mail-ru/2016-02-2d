package bomberman.mechanics.tiles.behaviors;

import bomberman.mechanics.World;
import bomberman.mechanics.interfaces.EventStashable;

public class NullBehavior extends ActionTileAbstractBehavior {
    public NullBehavior(World eventList) {
        super(eventList);
    }

    @Override
    public void behave(float deltaTime) {
        // T_T
    }
}
