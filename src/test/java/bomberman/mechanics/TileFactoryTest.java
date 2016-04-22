package bomberman.mechanics;

import bomberman.mechanics.interfaces.EntityType;
import bomberman.mechanics.interfaces.ITile;
import bomberman.mechanics.tiles.ActionTile;
import bomberman.mechanics.tiles.DestructibleWall;
import bomberman.mechanics.tiles.UndestructibleWall;
import bomberman.mechanics.tiles.behaviors.NullBehavior;
import bomberman.mechanics.tiles.functors.NullFunctor;
import constants.Constants;
import org.junit.Test;

import static org.junit.Assert.*;

public class TileFactoryTest {

    @Test
    public void testGetNewDestructibleWall() throws Exception {
        final TileFactory factory = TileFactory.getInstance();
        final ITile tile = factory.getNewTile(EntityType.DESTRUCTIBLE_WALL, 0);

        assertNotNull(tile);
        assertEquals(new DestructibleWall(0), tile);
    }

    @Test
    public void testGetNewUndestructibleWall() throws Exception {
        final TileFactory factory = TileFactory.getInstance();
        final ITile tile = factory.getNewTile(EntityType.UNDESTRUCTIBLE_WALL, 0);

        assertNotNull(tile);
        assertEquals(new UndestructibleWall(0), tile);
    }

    @Test
    public void testGetNewRangeBonus() throws Exception {
        final TileFactory factory = TileFactory.getInstance();
        final ITile tile = factory.getNewTile(EntityType.BONUS_INCMAXRANGE, Constants.GameMechanicsMocks.getEventStashable(), 0);

        assertNotNull(tile);
        assertEquals(new ActionTile(0, new NullFunctor(Constants.GameMechanicsMocks.getEventStashable()), new NullBehavior(Constants.GameMechanicsMocks.getEventStashable()), EntityType.BONUS_INCMAXRANGE), tile);
    }

    @Test
    public void testGetNewSpawnTimeBonus() throws Exception {
        final TileFactory factory = TileFactory.getInstance();
        final ITile tile = factory.getNewTile(EntityType.BONUS_DECBOMBSPAWN, Constants.GameMechanicsMocks.getEventStashable(), 0);

        assertNotNull(tile);
        assertEquals(new ActionTile(0, new NullFunctor(Constants.GameMechanicsMocks.getEventStashable()), new NullBehavior(Constants.GameMechanicsMocks.getEventStashable()), EntityType.BONUS_DECBOMBSPAWN), tile);
    }

    @Test
    public void testGetNewExplosionTimeBonus() throws Exception {
        final TileFactory factory = TileFactory.getInstance();
        final ITile tile = factory.getNewTile(EntityType.BONUS_DECBOMBFUSE, Constants.GameMechanicsMocks.getEventStashable(), 0);

        assertNotNull(tile);
        assertEquals(new ActionTile(0, new NullFunctor(Constants.GameMechanicsMocks.getEventStashable()), new NullBehavior(Constants.GameMechanicsMocks.getEventStashable()), EntityType.BONUS_DECBOMBFUSE), tile);
    }

    @Test
    public void testGetNewBomb() throws Exception {
        final TileFactory factory = TileFactory.getInstance();
        final ITile tile = factory.getNewTile(EntityType.BOMB, Constants.GameMechanicsMocks.getEventStashable(), Constants.GameMechanicsMocks.getBomberman(), 0);

        assertNotNull(tile);
        assertEquals(tile, tile);
    }
}