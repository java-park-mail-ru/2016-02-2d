package bomberman;

import bomberman.interfaces.IWorldBuilder;
import bomberman.interfaces.WorldType;
import bomberman.worldbuilders.BasicWorldBuilder;

public class WorldBuilderForeman {

    public static IWorldBuilder getWorldBuilderInstance(WorldType type){
        switch (type)
        {
            case BASIC_WORLD:
                return basicWorldBuilder;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static IWorldBuilder basicWorldBuilder = new BasicWorldBuilder();
}
