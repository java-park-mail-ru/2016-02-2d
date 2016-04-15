package main.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class Context {

    public void put(Class<?> clazz, Object object) throws InstantiationException {
        if (!contextObj.containsKey(clazz))
            contextObj.put(clazz, object);
        else {
            LOGGER.error("Could not add \"" + clazz.toString() + "\" class to contextObj! \t — already contained!");
            throw new InstantiationException();
        }
    }

    public Object get(Class<?> clazz) {
        return contextObj.get(clazz);
    }

    private static final Logger LOGGER = LogManager.getLogger(Context.class);
    private final Map<Class<?>, Object> contextObj = new HashMap<>();
}
