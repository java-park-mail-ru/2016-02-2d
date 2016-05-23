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
        if (BUILDERS.containsKey(worldType))
            return BUILDERS.get(worldType);
        else {
            LOGGER.warn("Cannot find \"" + worldType + "\" template! Returning empty basic world instead.");
            return new BasicWorldBuilder();
        }
    }

    public static String getRandomWorldName() {
        final Set<Map.Entry<String, IWorldBuilder>> builderSet = BUILDERS.entrySet();
        final ArrayList<String> names = builderSet.stream().map(Map.Entry::getKey).collect(Collectors.toCollection(ArrayList::new));

        final Random randomInt = new Random(new Date().hashCode());
        return names.get(Math.abs(randomInt.nextInt()) % names.size());
    }

    private static final Map<String, IWorldBuilder> BUILDERS = TextWorldBuilder.getAllTextBuilders();
    private static final Logger LOGGER = LogManager.getLogger(WorldBuilderForeman.class);
}
