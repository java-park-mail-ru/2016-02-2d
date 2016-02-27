package bomberman;

import bomberman.interfaces.IWorldBuilder;
import bomberman.interfaces.WorldType;
import bomberman.worldbuilders.BasicWorldBuilder;
import bomberman.worldbuilders.TextWorldBuilder;

import javax.inject.Singleton;

public class WorldBuilderForeman {

    public static IWorldBuilder getWorldBuilderInstance(WorldType type){
        switch (type)
        {
            case BASIC_WORLD:
                return basicWorldBuilder;
            case TEXT_SPIRAL_WORLD:
                return new TextWorldBuilder(type);
            default:
                throw new IllegalArgumentException();
        }
    }

    private static IWorldBuilder basicWorldBuilder = new BasicWorldBuilder();
}
