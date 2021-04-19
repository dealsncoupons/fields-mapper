package works.hop.fields.mapper.sample;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FieldMapperUtils {

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

    public static <T>T newInstance(Class<T> type){
        try {
            return type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(String.format("Could not create new instance of type %s", type.getName()), e);
        }
    }
}
