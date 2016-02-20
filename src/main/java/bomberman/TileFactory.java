package bomberman;

import bomberman.interfaces.EntityType;
import bomberman.interfaces.ITile;
import bomberman.tiles.UndestuctibleWall;

public class TileFactory {
    public static TileFactory GetInstance()
    {
        return singleton;
    }

    public ITile getNewTile(EntityType type, int id, int x, int y)
    {
        return new UndestuctibleWall(id, x, y);
    }

    private static TileFactory singleton = new TileFactory();
}
