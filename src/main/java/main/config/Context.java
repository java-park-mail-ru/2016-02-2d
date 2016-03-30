package main.config;

import java.util.HashMap;
import java.util.Map;

public class Context {

    public void put(Class<?> clazz, Object object) throws InstantiationException {
        if (!contextObj.containsKey(clazz))
            contextObj.put(clazz, object);
        else {
            System.out.format("Could not add \"%s\" class to contextObj! \t — already contained!", clazz.toString());
            throw new InstantiationException();
        }
    }

    public Object get(Class<?> clazz) {
        return contextObj.get(clazz);
    }

    private final Map<Class<?>, Object> contextObj = new HashMap<>();
}
