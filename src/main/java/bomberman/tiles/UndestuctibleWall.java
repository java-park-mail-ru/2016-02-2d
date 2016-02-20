package bomberman.tiles;

import bomberman.interfaces.EntityType;

public class UndestuctibleWall extends AbstractTile{
    public UndestuctibleWall(int id, int x, int y) {
        super(id, x, y);
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
}
