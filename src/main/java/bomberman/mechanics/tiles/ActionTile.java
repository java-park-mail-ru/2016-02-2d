package bomberman.mechanics.tiles;

import bomberman.mechanics.Bomberman;
import bomberman.mechanics.interfaces.EntityType;
import bomberman.mechanics.tiles.behaviors.ActionTileAbstractBehavior;
import bomberman.mechanics.tiles.functors.ActionTileAbstractFunctor;

public class ActionTile extends AbstractTile {

    public ActionTile(int id, ActionTileAbstractFunctor functor, ActionTileAbstractBehavior behavior, EntityType entityType ) {
        super(id);
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
    public void update(long deltaT) {
        behavior.behave(deltaT);
    }

    @Override
    public void applyAction(Bomberman bomberman) {
        functor.applyAction(bomberman);
    }

    public void markForDestruction() {
        shouldBeDestroyed = true;
    }

    @Override
    public boolean isDestructible() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ActionTile another = (ActionTile) o;

        return entityType == another.entityType;

    }

    @Override
    public int hashCode() {
        return entityType.hashCode();
    }

    private boolean shouldBeDestroyed;
    private final ActionTileAbstractFunctor functor;
    private final ActionTileAbstractBehavior behavior;
    private final EntityType entityType;
}
