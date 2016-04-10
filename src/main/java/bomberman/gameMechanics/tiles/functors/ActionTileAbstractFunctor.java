package bomberman.gameMechanics.tiles.functors;

import bomberman.gameMechanics.interfaces.Actable;
import bomberman.gameMechanics.interfaces.EventStashable;
import bomberman.gameMechanics.tiles.ActionTile;

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
