package bomberman.tiles.functors;

import bomberman.interfaces.Actable;
import bomberman.interfaces.EventStashable;
import bomberman.tiles.ActionTile;

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
