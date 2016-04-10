package bomberman.gameMechanics.tiles.behaviors;

import bomberman.gameMechanics.interfaces.EventStashable;

public class NullBehavior extends ActionTileAbstractBehavior {
    public NullBehavior(EventStashable eventList) {
        super(eventList);
    }

    @Override
    public void behave(float deltaTime) {
        // T_T
    }
}
