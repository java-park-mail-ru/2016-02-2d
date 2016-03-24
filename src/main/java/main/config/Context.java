package main.config;

import java.util.HashMap;
import java.util.Map;

public class Context {

    public void put(Class<?> clazz, Object object) {
        if (!context.containsKey(clazz))
            context.put(clazz, object);
        else
            return;
    }

    public Object get(Class<?> clazz) {
        return context.get(clazz);
    }

    private Map<Class<?>, Object> context = new HashMap<>();
}
