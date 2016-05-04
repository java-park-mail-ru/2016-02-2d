package bomberman.mechanics.interfaces;

import bomberman.mechanics.worldbuilders.WorldData;

public interface IWorldBuilder {
    WorldData getWorldData(UniqueIDManager supplicant, EventStashable eventQueue);
}
