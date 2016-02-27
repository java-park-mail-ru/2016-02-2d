package bomberman.worldbuilders;

import bomberman.TileFactory;
import bomberman.interfaces.*;
import bomberman.worldbuilders.blueprints.SpiralWorldBlueprint;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class TextWorldBuilder extends IWorldBuilder {

    public TextWorldBuilder(WorldType worldType){
        switch (worldType)
        {
            case TEXT_SPIRAL_WORLD:
                blueprint = new SpiralWorldBlueprint();
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public ITile[][] getITileArray(int height, int width, UniqueIDManager supplicant, EventStashable eventQueue) {
        this.supplicant = supplicant;
        this.eventQueue = eventQueue;
        generateWorldFromText();
        return tileArray;
    }

    @Override
    public float[][] getBombermenSpawns() {
        if (spawnList.isEmpty())
            generateWorldFromText();

        float[][] spawnArray = new float[spawnList.size()][2];  // 2 for x and y coordinates
        int i = 0;

        for(float[] onePoint : spawnList)
        {
            spawnArray[i] = onePoint;
            i++;
        }
        return spawnArray;
    }

    private void generateWorldFromText() {
        tileArray = new ITile[blueprint.getHeight()][blueprint.getWidth()];

        int y = 0;
        for (String row : blueprint.getBlueprint())
        {
            int x = 0;
            for (char tileChar : row.toCharArray())
            {
                tileArray[y][x] = mapSymbolToTile(tileChar, x, y);
                x++;
            }
            y++;
        }
    }

    @Nullable
    private ITile mapSymbolToTile(char c, int x, int y){
        switch (c)
        {
            case '.':
                return null;
            case '#':
                return TileFactory.getInstance().getNewTile(EntityType.UNDESTRUCTIBLE_WALL, supplicant.getNextID(), x, y);
            case '+':
                return TileFactory.getInstance().getNewTile(EntityType.DESTRUCTIBLE_WALL, supplicant.getNextID(), x, y);
            case 'H':
                return TileFactory.getInstance().getNewTile(EntityType.BONUS_HEAL, eventQueue, supplicant.getNextID(), x, y);
            case 'S':
                spawnList.add(new float[]{x, y});
                return null;
            default:    //TODO: Assign all bonuses to symbols!
                throw new IllegalArgumentException();
        }
    }


    private Blueprintable blueprint;
    private UniqueIDManager supplicant;
    private EventStashable eventQueue;
    private ITile[][] tileArray;
    private Queue<float[]> spawnList = new LinkedList<>();

}
