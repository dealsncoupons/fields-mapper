package works.hop.fields.mapper.sample;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class FieldMapper3 {

    private final Map<String, FieldInfo3<?, ?>> mapping = new HashMap<>();
    private final Map<Class<?>, ClassInfo3<?, ?>> classMapping = new HashMap<>();

    public void map(String source, String target) {
        this.mapping.put(source, new FieldInfo3<>(source, target));
    }

    public <T, R> void map(String source, String target, Function<T, R> resolver) {
        this.mapping.put(source, new FieldInfo3<>(source, target, resolver));
    }

    public void map(Class<?> source, Class<?> target) {
        this.classMapping.put(source, new ClassInfo3<>(source, target));
    }

    public void map(Class<?> source, Class<?> target, String prefix) {
        this.classMapping.put(source, new ClassInfo3<>(source, target, prefix));
    }

    public <A, B> Function<A, B> resolver(String source) {
        return this.mapping.containsKey(source) ? (Function<A, B>) this.mapping.get(source).resolver : null;
    }

    public Optional<String> targetProperty(String property) {
        if (mapping.containsKey(property)) {
            return Optional.ofNullable(mapping.get(property).target);
        }
        return Optional.empty();
    }

    public Optional<Class<?>> targetClass(Class<?> aClass) {
        if (classMapping.containsKey(aClass)) {
            return Optional.ofNullable(classMapping.get(aClass).target);
        }
        return Optional.empty();
    }

    public String prefix(Class<?> aClass) {
        return classMapping.containsKey(aClass) ? classMapping.get(aClass).prefix : "";
    }
}
