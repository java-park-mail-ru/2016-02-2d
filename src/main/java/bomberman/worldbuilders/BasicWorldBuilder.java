package bomberman.worldbuilders;


import bomberman.TileFactory;
import bomberman.interfaces.*;

import javax.inject.Singleton;

@Singleton
public class BasicWorldBuilder extends IWorldBuilder {

    @Override
    public ITile[][] getITileArray(UniqueIDManager supplicant, EventStashable eventQueue) {
        // Array has dimensions YxX because java multi-dimensional arrays are arrays of arrays.
        // This means [first][] bracket pair is number of array we want to access.
        // And [][second] bracket pair is true position of an element we want to access.
        // Thus [Y][X] element means Xth element in Yth row.
        final ITile[][] tileArray = new ITile[DEFAULT_WORLD_HEIGHT][DEFAULT_WORLD_WIDTH];

        // Filling left and right borders
        for (int j = 0; j <= tileArray.length; ++j)
        {
            TileFactory.getInstance().getNewTile(EntityType.UNDESTRUCTIBLE_WALL, supplicant.getNextID(), 0, j);
            TileFactory.getInstance().getNewTile(EntityType.UNDESTRUCTIBLE_WALL, supplicant.getNextID(), DEFAULT_WORLD_WIDTH - 1, j);
        }                                                                                                                //    ^-- Thanks this is not a magic number. ^_^

        // Filling top and bottom borders except for first and las columns: something is already there.
        for (int i = 1; i <= tileArray[0].length - 1; ++i)
        {
            TileFactory.getInstance().getNewTile(EntityType.UNDESTRUCTIBLE_WALL, supplicant.getNextID(), i, 0);
            TileFactory.getInstance().getNewTile(EntityType.UNDESTRUCTIBLE_WALL, supplicant.getNextID(), i, DEFAULT_WORLD_HEIGHT - 1);
        }

        return tileArray;
    }

    @Override// x y
    public float[][] getBombermenSpawns() {
        return new float[][]{{1.0f, 1.0f},{1.0f, (float)(DEFAULT_WORLD_HEIGHT - 1)},{(float)(DEFAULT_WORLD_WIDTH - 1), 1.0f},{(float)(DEFAULT_WORLD_HEIGHT - 1), (float)(DEFAULT_WORLD_WIDTH - 1)}};
    }
}
