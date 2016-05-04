package bomberman.mechanics.interfaces;

import bomberman.mechanics.World;
import bomberman.mechanics.worldbuilders.WorldData;

public interface IWorldBuilder {
    WorldData getWorldData(World supplicant);
}
