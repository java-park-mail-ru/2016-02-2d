package bomberman.mechanics.worldbuilders;

import bomberman.mechanics.interfaces.ITile;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Frog on 04.05.2016.
 */
public class WorldData {
    WorldData (ITile[][] tileArray, float[][] spawnArray, String name) {
        this.tileArray = tileArray;
        this.spawnArray = spawnArray;
        this.name = name;
    }

    public ITile[][] getTileArray() {
        return tileArray;
    }

    public float[][] getSpawnList() {
        return spawnArray;
    }

    public String getName() {
        return name;
    }

    private final ITile[][] tileArray;
    private final float[][] spawnArray;
    private final String name;
}
