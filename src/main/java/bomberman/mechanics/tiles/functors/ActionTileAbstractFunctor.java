package bomberman.mechanics.tiles.functors;

import bomberman.mechanics.interfaces.Actable;
import bomberman.mechanics.interfaces.EventStashable;
import bomberman.mechanics.tiles.ActionTile;

public abstract class ActionTileAbstractFunctor implements Actable{
    public ActionTileAbstractFunctor(EventStashable eventList){
        this.eventList = eventList;
    }

    public void linkWithTile(ActionTile newOwner)
    {
       owner = newOwner;
    }


    protected ActionTile owner;
    protected final EventStashable eventList;
}