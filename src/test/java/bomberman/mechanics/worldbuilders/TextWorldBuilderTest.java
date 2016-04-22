package bomberman.mechanics.worldbuilders;

import bomberman.mechanics.interfaces.ITile;
import bomberman.mechanics.interfaces.IWorldBuilder;
import bomberman.mechanics.tiles.DestructibleWall;
import bomberman.mechanics.tiles.UndestructibleWall;
import constants.Constants;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class TextWorldBuilderTest {

    @BeforeClass
    public static void setup() throws Exception {
        testWorldBuilder = new TextWorldBuilder(new File("data/unit-test-world-do-not-alter.txt"));
    }

    @Test
    public void testGetITileArray() throws Exception {
        final ITile[][] actualTileArray = testWorldBuilder.getITileArray(Constants.GameMechanicsMocks.getUniqueIDManager(), Constants.GameMechanicsMocks.getEventStashable());
        final ITile[][] expectedTileArray = new ITile[32][32];
        for (int j = 0; j < expectedTileArray.length; ++j)
            for (int i = 0; i < expectedTileArray[i].length; ++i)
                expectedTileArray[j][i] = null;
        expectedTileArray[0][0] = new UndestructibleWall(0);
        expectedTileArray[0][1] = new UndestructibleWall(1);
        expectedTileArray[1][0] = new UndestructibleWall(2);
        expectedTileArray[2][2] = new DestructibleWall(3);

        assertEquals(expectedTileArray.length, actualTileArray.length);
        for (int i = 0; i < expectedTileArray.length; ++i)
            assertArrayEquals(expectedTileArray[i], actualTileArray[i]);
    }

    @Test
    public void testGetBombermenSpawns() throws Exception {
        final float[][] actualSpawns = testWorldBuilder.getBombermenSpawns();
        final float[][] expectedSpawns = new float[][] {{31.5f, 31.5f}};

        assertEquals(expectedSpawns.length, actualSpawns.length);
        for (int i = 0; i < expectedSpawns.length; ++i)
            assertArrayEquals(expectedSpawns[i], actualSpawns[i], SOME_ERROR_DELTA);
    }

    @Test
    public void testGetName() throws Exception {
        final String worldName = testWorldBuilder.getName();
        assertEquals("Unit test", worldName);
    }

    private static IWorldBuilder testWorldBuilder;
    private static final float SOME_ERROR_DELTA = 10e-3f;
}