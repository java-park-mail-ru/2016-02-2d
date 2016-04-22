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
    public boolean shouldBeDestroyed() {
        return false;   // Bad point in design.     It should be to be destructed once bomb ray hits it. But since this tile is removed from game after bomb ray hits it, there is no need for information to decide whether to destroy it or not – it already has been destroyed! If only action tiles fully use this feature, what is the reason for this method to exist in other tiles? Interface compatibility...
    }

    @Override
    public EntityType getType() {
        return EntityType.DESTRUCTIBLE_WALL;
    }
}
