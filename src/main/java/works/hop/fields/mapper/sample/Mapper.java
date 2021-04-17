package works.hop.fields.mapper.sample;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class Mapper {

    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, String> fields = new HashMap<>();
    private final Map<String, Function> resolvers = new HashMap<>();

    public void field(String left, String right) {
        this.fields.put(left, right);
    }

    public <T, R> void field(String left, String right, Function<T, R> resolver) {
        this.field(left, right);
        this.resolvers.put(right, resolver);
    }

    public <T> void copy(String field, T value) {
        System.out.printf("the field '%s' has value '%s'%n", field, value);
        values.put(field, value);
    }

    public void map(String field, Object target) {
        String property = fields.getOrDefault(field, field);
        map(property, field, target.getClass(), target);
    }

    private void map(String property, String valueKey, Class<?> type, Object target) {
        if (property.contains(".")) {
            int index = property.indexOf(".");
            String field = property.substring(0, index);
            String getter = String.format("get%s%s", Character.toUpperCase(field.charAt(0)), field.substring(1));
            try {
                Method method = target.getClass().getMethod(getter);
                map(property.substring(index + 1), valueKey, method.getReturnType(), method.invoke(target));
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            String setter = String.format("set%s%s", Character.toUpperCase(property.charAt(0)), property.substring(1));
            try {
                Object value;
                if (resolvers.containsKey(valueKey)) {
                    value = resolvers.get(valueKey).apply(values.get(valueKey));
                    Method method;
                    if (List.class.isAssignableFrom(value.getClass())) {
                        method = type.getMethod(setter, List.class);
                    } else if (Set.class.isAssignableFrom(value.getClass())) {
                        method = type.getMethod(setter, Set.class);
                    } else if (Map.class.isAssignableFrom(value.getClass())) {
                        method = type.getMethod(setter, Map.class);
                    } else {
                        method = type.getMethod(setter, value.getClass());
                    }
                    method.invoke(target, value);
                } else {
                    value = values.get(valueKey);
                    Method method = type.getMethod(setter, value.getClass());
                    method.invoke(target, value);
                }
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }
}
