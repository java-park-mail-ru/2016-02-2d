package bomberman.tiles.behaviors;


import bomberman.interfaces.EventStashable;
import bomberman.tiles.ActionTile;

public abstract class ActionTileAbstractBehavior {
    public ActionTileAbstractBehavior(EventStashable eventList)
    {
        this.eventList = eventList;
    }

    public void linkWithTile(ActionTile owner)
    {
        this.owner = owner;
    }

    public abstract void behave(float deltaTime);

    protected ActionTile owner;
    protected EventStashable eventList;
}
