package main.config;

import java.util.HashMap;
import java.util.Map;

public class Context {

    public void put(Class<?> clazz, Object object) throws InstantiationException {
        if (!context.containsKey(clazz))
            context.put(clazz, object);
        else {
            System.out.format("Could not add \"%s\" class to context! \t — already contained!", clazz.toString());
            throw new InstantiationException();
        }
    }

    public Object get(Class<?> clazz) {
        return context.get(clazz);
    }

    private final Map<Class<?>, Object> context = new HashMap<>();
}
