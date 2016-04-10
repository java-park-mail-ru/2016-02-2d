package bomberman.gameMechanics.tiles.behaviors;


import bomberman.gameMechanics.interfaces.EventStashable;
import bomberman.gameMechanics.tiles.ActionTile;

public abstract class ActionTileAbstractBehavior {
    public ActionTileAbstractBehavior(EventStashable eventList)
    {
        this.eventList = eventList;
    }

    public void linkWithTile(ActionTile newOwner)
    {
        owner = newOwner;
    }

    public abstract void behave(float deltaTime);

    protected ActionTile owner;
    protected final EventStashable eventList;
}
