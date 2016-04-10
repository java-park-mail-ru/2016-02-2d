package bomberman.mechanics.interfaces;

// Can I call it interface with "safe" default implementation?

public abstract class IWorldBuilder {
    public ITile[][] getITileArray(UniqueIDManager supplicant, EventStashable eventQueue) { throw new UnsupportedOperationException(); }
    public ITile[][] getITileArray(int height, int width, UniqueIDManager supplicant, EventStashable eventQueue) { throw new UnsupportedOperationException(); }
    public float[][] getBombermenSpawns() { throw new UnsupportedOperationException(); }

    public static final int DEFAULT_WORLD_HEIGHT = 16;
    public static final int DEFAULT_WORLD_WIDTH = 32;
}
