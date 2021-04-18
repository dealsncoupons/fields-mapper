package works.hop.fields.mapper.config;

import java.util.HashMap;

public class PerRequestCache extends HashMap<String, Object> {

    public <V> void putValue(String key, V value) {
        super.put(key, value);
    }

    public <V> V putValueIfAbsent(String key, V value) {
        return (V) super.putIfAbsent(key, value);
    }

    public <V> V getValue(String key) {
        return (V) super.get(key);
    }

    public <V> V getOrDefaultValue(String key, V value) {
        return (V) super.getOrDefault(key, value);
    }

    public <V> V removeValue(String key) {
        return (V) super.remove(key);
    }

    public <V, U> V replaceValue(String key, U value) {
        return (V) super.replace(key, value);
    }
}
