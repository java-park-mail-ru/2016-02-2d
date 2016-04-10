package bomberman.mechanics;

import bomberman.mechanics.interfaces.IWorldBuilder;
import bomberman.mechanics.interfaces.WorldType;
import bomberman.mechanics.worldbuilders.BasicWorldBuilder;
import bomberman.mechanics.worldbuilders.TextWorldBuilder;

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
