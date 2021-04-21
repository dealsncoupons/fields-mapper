package works.hop.fields.mapper.sample;

import com.google.common.primitives.Primitives;
import ma.glasnost.orika.MapEntry;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FieldMapperUtils {

    public static Map<String, Function<Object, Object>> valueSuppliers(Object source, Class<?> type, String prefix) {
        Map<String, Function<Object, Object>> fields = Arrays.stream(type.getDeclaredFields()).filter(field ->
                !Modifier.isStatic(field.getModifiers())
        ).map(field -> {
            Class<?> fieldType = field.getType();
            Object fieldValue = FieldMapperUtils.getFieldValue(field, source);
            int modifiers = fieldType.getModifiers();
            if (fieldValue != null) {
                if (!Modifier.isStatic(modifiers) && (fieldType.isPrimitive() || Primitives.isWrapperType(fieldType) || String.class.isAssignableFrom(fieldType))) {
                    return new MapEntry<>(String.format("%s.%s", prefix, field.getName()), (Function<Object, Object>) o -> fieldValue);
                } else if (List.class.isAssignableFrom(fieldType)) {
                    List<?> sourceList = (List) fieldValue;
                    AtomicInteger index = new AtomicInteger(0);
                    Map<String, Function<Object, Object>> listEntries = new HashMap<>();
                    sourceList.forEach(listItem -> {
                        Map<String, Function<Object, Object>> listItemEntries = valueSuppliers(
                                listItem, listItem.getClass(), String.format("%s(%d)", prefix, index.getAndIncrement())) ;
                        listEntries.putAll(listItemEntries);

                    });
                    return new MapEntry<>(String.format("%s.%s", prefix, field.getName()), (Function<Object, Object>) o -> listEntries);
                } else if (Map.class.isAssignableFrom(fieldType)) {
                    Map<?, ?> sourceMap = (Map) fieldValue;
                    Map<String, Function<Object, Object>> entrySetValues = new HashMap<>();
                    sourceMap.entrySet().forEach(entryItem -> {
                        Object entryKey = entryItem.getKey();
                        Map<String, Function<Object, Object>> entryKeyItems = valueSuppliers(
                                entryKey, entryKey.getClass(), String.format("%s.%s[%s:]", prefix, field.getName(), entryKey)) ;
                        entrySetValues.putAll(entryKeyItems);

                        Object entryValue = entryItem.getValue();
                        Map<String, Function<Object, Object>> entryValueItems = valueSuppliers(
                                entryValue, entryValue.getClass(), String.format("%s.%s[:%s]", prefix, field.getName(), entryKey)) ;
                        entrySetValues.putAll(entryValueItems);

                    });
                    return new MapEntry<>(String.format("%s.%s", prefix, field.getName()), (Function<Object, Object>) o -> entrySetValues);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toMap(MapEntry::getKey, MapEntry::getValue));
        if (type.getSuperclass() != Object.class) {
            fields.putAll(valueSuppliers(source, type.getSuperclass(), prefix));
        }
        return fields;
    }

    public static void instanceFields(Class<?> type, Map<Class<?>, List<Field>> fields) {
        fields.put(type, Arrays.stream(type.getDeclaredFields()).filter(field ->
                !Modifier.isStatic(field.getModifiers())
        ).collect(Collectors.toList()));
        if (type.getSuperclass() != Object.class) {
            instanceFields(type.getSuperclass(), fields);
        }
    }

    public static Field instanceField(Class<?> type, String property) {
        if (property.matches("^[\\w_$]+$")) {
            try {
                return type.getDeclaredField(property);
            } catch (NoSuchFieldException e) {
                if (type.getSuperclass() != Object.class) {
                    return instanceField(type.getSuperclass(), property);
                }
                return null;
            }
        } else {
            int delimiterIndex = property.indexOf(".");
            if (delimiterIndex != -1) {
                //must be nested property
                String parentProperty = property.substring(0, delimiterIndex);
                Field parentField = instanceField(type, parentProperty);
                if (parentField != null) {
                    return instanceField(parentField.getType(), property.substring(delimiterIndex + 1));
                } else {
                    throw new RuntimeException(String.format("Missing '%s' property name in '%s' class", parentProperty, type.getName()));
                }
            } else {
                throw new RuntimeException(String.format("%s - %s", "Unexpected field definition", property));
            }
        }
    }

    public static void setFieldValue(Field field, Object target, Object value) {
        boolean isAccessible = field.canAccess(target);
        field.setAccessible(true);
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            field.setAccessible(isAccessible);
        }
    }

    public static Object getFieldValue(Field field, Object target) {
        boolean isAccessible = field.canAccess(target);
        field.setAccessible(true);
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } finally {
            field.setAccessible(isAccessible);
        }
    }

    public static <T> T newInstance(Class<T> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("Could not create new instance of type %s", type.getName()), e);
        }
    }
}
