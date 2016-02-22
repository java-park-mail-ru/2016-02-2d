package bomberman.interfaces;

// Can I call it interface with "safe" default implementation?

public abstract class IWorldBuilder {
    public ITile[][] getITileArray(UniqueIDManager supplicant) { throw new UnsupportedOperationException(); }
    public ITile[][] getITileArray(int height, int width, UniqueIDManager supplicant) { throw new UnsupportedOperationException(); }
    public float[][] getBombermenSpawn() { throw new UnsupportedOperationException(); }

    public static final int DEFAULT_WORLD_HEIGHT = 16;
    public static final int DEFAULT_WORLD_WIDTH = 32;
}
