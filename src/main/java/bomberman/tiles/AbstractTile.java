package bomberman.tiles;

import bomberman.Bomberman;
import bomberman.interfaces.ITile;

public abstract class AbstractTile implements ITile{

    public AbstractTile(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    @Override
    public int[] getCoordinates() {
        return new int[]{x, y};
    }

    // May be overriden later.
    @Override
    public void applyAction(Bomberman bomberman) {
        // Nothing to do
    }

    // Should be overriden later.
    //@Override
    //public EntityType getType() {
    //   return 0;
    //}

    @Override
    public int getID() {
        return id;
    }

    // May be overriden later.
    @Override
    public boolean isDestuctible() {
        return false;
    }

    // May be overriden later.
    @Override
    public boolean isPassable() {
        return true;
    }

    // May be overriden later.
    @Override
    public void update(float deltaTime) {
        // Nothing to do
    }

    // Should be overriden later.
    //@Override
    //public boolean shouldBeDestroyed() {
    //   return 0;
    //}

    private int id;
    private int x;
    private int y;
}
