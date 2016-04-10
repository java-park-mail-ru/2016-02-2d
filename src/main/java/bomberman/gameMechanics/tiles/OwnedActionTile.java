package bomberman.gameMechanics.tiles;

import bomberman.gameMechanics.Bomberman;
import bomberman.gameMechanics.interfaces.EntityType;
import bomberman.gameMechanics.interfaces.Ownable;
import bomberman.gameMechanics.tiles.behaviors.ActionTileAbstractBehavior;
import bomberman.gameMechanics.tiles.functors.ActionTileAbstractFunctor;

public class OwnedActionTile extends ActionTile implements Ownable {
    public OwnedActionTile(int id, int x, int y, ActionTileAbstractFunctor functor, ActionTileAbstractBehavior behavior, EntityType entityType, Bomberman owner) {
        super(id, x, y, functor, behavior, entityType);
        this.owner = owner;
    }

    @Override
    public Bomberman getOwner() {
        return owner;
    }

    private final Bomberman owner;
}

// This class is giant, huge KOCTblJIb!!!
//
//      __              __
//      \ \            / /
//       \ \__________/ /
//        \  ________  /
//         \ \      / /
//          | |    | |
//          | |    | |
//          | |    | |
//          | |    | |
//          | |    | |
//          | |    | |
//          | |    | |
//          \ \____/ /
//           \  __  /
//            ||  ||
//            |\__/|
//             \  /
//              ||
//              ||
//              ||
//              ||
//              ||
//              ||
//              ||
//              ||
//              ||
//             \__/