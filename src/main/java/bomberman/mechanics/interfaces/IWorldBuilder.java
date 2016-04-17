package bomberman.mechanics.interfaces;

// Can I call it interface with "safe" default implementation?

public interface IWorldBuilder {
    ITile[][] getITileArray(UniqueIDManager supplicant, EventStashable eventQueue);
    float[][] getBombermenSpawns();
    String getName();
}
