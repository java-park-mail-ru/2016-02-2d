package bomberman.gameMechanics;

import bomberman.gameMechanics.interfaces.IWorldBuilder;
import bomberman.gameMechanics.interfaces.WorldType;
import bomberman.gameMechanics.worldbuilders.BasicWorldBuilder;
import bomberman.gameMechanics.worldbuilders.TextWorldBuilder;

public class WorldBuilderForeman {

    public static IWorldBuilder getWorldBuilderInstance(WorldType type){
        switch (type)
        {
            case BASIC_WORLD:
                return BASIC_WORLD_BUILDER;
            case TEXT_SPIRAL_WORLD:
                return new TextWorldBuilder(type);
            default:
                throw new IllegalArgumentException();
        }
    }

    private static final IWorldBuilder BASIC_WORLD_BUILDER = new BasicWorldBuilder();
}
