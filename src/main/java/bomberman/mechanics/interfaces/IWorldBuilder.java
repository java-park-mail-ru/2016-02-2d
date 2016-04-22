package bomberman.mechanics.interfaces;

import org.javatuples.Triplet;

public interface IWorldBuilder {
    Triplet<ITile[][], float[][], String> getWorldData(UniqueIDManager supplicant, EventStashable eventQueue);
}
