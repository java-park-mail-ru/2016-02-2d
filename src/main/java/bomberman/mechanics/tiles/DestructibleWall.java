package bomberman.mechanics.tiles;

import bomberman.mechanics.interfaces.EntityType;

public class DestructibleWall extends AbstractTile {
    public DestructibleWall(int id) {
        super(id);
    }

    @Override
    public boolean isDestructible() {
        return true;
    }

    @Override
    public boolean isPassable() {
        return false;
    }

    @Override
    public boolean shouldSpawnBonusOnDestruction() {
        return true;
    }

    @Override
    public EntityType getType() {
        return EntityType.DESTRUCTIBLE_WALL;
    }

    @SuppressWarnings("QuestionableName")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DestructibleWall that = (DestructibleWall) o;

        return getType() == that.getType();

    }

    @Override
    public int hashCode() {
        return getType().hashCode();
    }

}
