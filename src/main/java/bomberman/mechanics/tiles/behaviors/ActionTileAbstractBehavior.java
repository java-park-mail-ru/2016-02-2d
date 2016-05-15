package bomberman.mechanics.tiles.behaviors;


import bomberman.mechanics.World;
import bomberman.mechanics.tiles.ActionTile;

public abstract class ActionTileAbstractBehavior {
    public ActionTileAbstractBehavior(World eventList)
    {
        this.eventList = eventList;
    }

    public void linkWithTile(ActionTile newOwner)
    {
        owner = newOwner;
    }

    public abstract void behave(long deltaT);

    protected ActionTile owner;
    protected final World eventList;
}
