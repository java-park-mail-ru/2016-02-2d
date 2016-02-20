package bomberman.tiles;

import bomberman.interfaces.EntityType;
import bomberman.tiles.behaviors.ActionTileAbstractBehavior;
import bomberman.tiles.functors.ActionTileAbstractFunctor;

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
    public boolean isDestuctible() {
        return super.isDestuctible();
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

    public void markForDestruction() {
        shouldBeDestroyed = true;
    }

    private boolean shouldBeDestroyed;
    private ActionTileAbstractFunctor functor;
    private ActionTileAbstractBehavior behavior;
    private EntityType entityType;
}
