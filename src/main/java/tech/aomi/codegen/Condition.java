package tech.aomi.codegen;

import java.util.HashMap;
import java.util.Map;

public class Condition extends HashMap<String, Object> {

    public Condition(String key, Object value) {
        Map<String, Object> c = new HashMap<>();
        c.put(key, value);
        this.put("c", c);
    }

}