package bomberman.mechanics;

import bomberman.mechanics.interfaces.IWorldBuilder;
import bomberman.mechanics.worldbuilders.BasicWorldBuilder;
import bomberman.mechanics.worldbuilders.TextWorldBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class WorldBuilderForeman {

    public static IWorldBuilder getWorldBuilderInstance(String worldType) {
        if (builders.containsKey(worldType))
            return builders.get(worldType);
        else {
            LOGGER.warn("Cannot find \"" + worldType + "\" template! Returning empty basic world instead.");
            return new BasicWorldBuilder();
        }
    }

    public static String getRandomWorldName() {
        final Set<Map.Entry<String, IWorldBuilder>> builderSet = builders.entrySet();
        final ArrayList<String> names = new ArrayList<>();

        for (Map.Entry<String, IWorldBuilder> entry: builderSet)
            names.add(entry.getKey());

        final Random randomInt = new Random(new Date().hashCode());
        return names.get(randomInt.nextInt() % names.size());
    }

    private static Map<String, IWorldBuilder> builders = TextWorldBuilder.getAllTextBuilders();
    private static final Logger LOGGER = LogManager.getLogger(WorldBuilderForeman.class);
}
