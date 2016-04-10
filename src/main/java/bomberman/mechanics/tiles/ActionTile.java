package bomberman.mechanics.tiles;

import bomberman.mechanics.Bomberman;
import bomberman.mechanics.interfaces.EntityType;
import bomberman.mechanics.tiles.behaviors.ActionTileAbstractBehavior;
import bomberman.mechanics.tiles.functors.ActionTileAbstractFunctor;

public class ActionTile extends AbstractTile {

    public ActionTile(int id, int x, int y, ActionTileAbstractFunctor functor, ActionTileAbstractBehavior behavior, EntityType entityType ) {
        super(id, x, y);
        this.functor = functor;
        functor.linkWithTile(this);
        this.behavior = behavior;
        behavior.linkWithTile(this);
        this.entityType = entityType;
        shouldBeDestroyed = false;
    }

    @Override
    public EntityType getType() {
        return entityType;
    }

    @Override
    public boolean shouldBeDestroyed() {
        return shouldBeDestroyed;
    }

    @Override
    public void update(float deltaTime) {
        behavior.behave(deltaTime);
    }

    @Override
    public void applyAction(Bomberman bomberman) {
        functor.applyAction(bomberman);
    }


    public void markForDestruction() {
        shouldBeDestroyed = true;
    }

    private boolean shouldBeDestroyed;
    private final ActionTileAbstractFunctor functor;
    private final ActionTileAbstractBehavior behavior;
    private final EntityType entityType;
}
