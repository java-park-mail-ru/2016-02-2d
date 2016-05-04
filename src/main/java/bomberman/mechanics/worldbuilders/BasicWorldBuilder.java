package bomberman.mechanics.worldbuilders;


import bomberman.mechanics.TileFactory;
import bomberman.mechanics.World;
import bomberman.mechanics.interfaces.*;
import org.javatuples.Triplet;

import javax.inject.Singleton;

@Singleton
public class BasicWorldBuilder implements IWorldBuilder {

    @Override
    public WorldData getWorldData(World supplicant) {
        return new WorldData(getITileArray(supplicant), getBombermenSpawns(), getName());
    }

    private ITile[][] getITileArray(World supplicant) {
        // Array has dimensions YxX because java multi-dimensional arrays are arrays of arrays.
        // This means [first][] bracket pair is number of array we want to access.
        // And [][second] bracket pair is true position of an element we want to access.
        // Thus [Y][X] element means Xth element in Yth row.
        final ITile[][] tileArray = new ITile[DEFAULT_WORLD_HEIGHT][DEFAULT_WORLD_WIDTH];

        // Filling left and right borders
        for (int j = 0; j < tileArray.length; ++j)
        {
            tileArray[j][0] = TileFactory.getInstance().getNewTile(EntityType.UNDESTRUCTIBLE_WALL, supplicant.getNextID());
            tileArray[j][DEFAULT_WORLD_HEIGHT - 1] = TileFactory.getInstance().getNewTile(EntityType.UNDESTRUCTIBLE_WALL, supplicant.getNextID());
        }

        // Filling top and bottom borders except for first and las columns: something is already there.
        for (int i = 1; i < tileArray[0].length - 1; ++i)
        {
            tileArray[0][i] = TileFactory.getInstance().getNewTile(EntityType.UNDESTRUCTIBLE_WALL, supplicant.getNextID());
            tileArray[DEFAULT_WORLD_WIDTH - 1][i] = TileFactory.getInstance().getNewTile(EntityType.UNDESTRUCTIBLE_WALL, supplicant.getNextID());
        }

        return tileArray;
    }

              // x y
    private float[][] getBombermenSpawns() {
        return new float[][]{{1.0f, 1.0f},{1.0f, (float)(DEFAULT_WORLD_HEIGHT - 1)},{(float)(DEFAULT_WORLD_WIDTH - 1), 1.0f},{(float)(DEFAULT_WORLD_HEIGHT - 1), (float)(DEFAULT_WORLD_WIDTH - 1)}};
    }

    private String getName() {
        return "REPORT AS A BUG";
    }

    private static final int DEFAULT_WORLD_HEIGHT = 32;
    private static final int DEFAULT_WORLD_WIDTH = 32;
}
