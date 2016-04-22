package bomberman.mechanics.tiles;

import bomberman.mechanics.interfaces.EntityType;

public class UndestructibleWall extends AbstractTile{
    public UndestructibleWall(int id) {
        super(id);
    }

    @Override
    public EntityType getType() {
        return EntityType.UNDESTRUCTIBLE_WALL;
    }

    @Override
    public boolean isPassable() {
        return false;
    }

    @Override
    public boolean shouldBeDestroyed() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final UndestructibleWall that = (UndestructibleWall) o;

        return getType() == that.getType();

    }

    @Override
    public int hashCode() {
        return getType().hashCode();
    }
}
