package bomberman.mechanics;

import bomberman.mechanics.interfaces.IWorldBuilder;
import bomberman.mechanics.worldbuilders.BasicWorldBuilder;
import bomberman.mechanics.worldbuilders.TextWorldBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

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
        final ArrayList<String> names = builderSet.stream().map(Map.Entry::getKey).collect(Collectors.toCollection(ArrayList::new));

        final Random randomInt = new Random(new Date().hashCode());
        return names.get(Math.abs(randomInt.nextInt()) % names.size());
    }

    private static Map<String, IWorldBuilder> builders = TextWorldBuilder.getAllTextBuilders();
    private static final Logger LOGGER = LogManager.getLogger(WorldBuilderForeman.class);
}
