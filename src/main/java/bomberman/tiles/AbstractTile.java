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
    public boolean isDestructible() {
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

    //
    // Describers
    //

    // How am I supposed to name these? certainAbstractTileIdentificator?
    // certainAbstractTileXCoordinate? certainAbstractTileYCoordinate? But they differ only in one position.
    // Having long names is much more confusing than simple self-describing "x".
    @SuppressWarnings("InstanceVariableNamingConvention")
    private final int id;
    @SuppressWarnings("InstanceVariableNamingConvention")
    private final int x;
    @SuppressWarnings("InstanceVariableNamingConvention")
    private final int y;
}
