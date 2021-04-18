package works.hop.fields.mapper.sample;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Setters {

    public static String setterMethod(String fieldName) {
        return String.format("set%s%s", Character.toUpperCase(fieldName.charAt(0)), fieldName.substring(1));
    }

    public static void setPropertyValue(String property, Object value, Object target) {
        setPropertyValueByMethod(property, value, target);
    }

    private static void setPropertyValueByMethod(String property, Object value, Object target) {
        String setter = setterMethod(property);
        try {
            Method method = target.getClass().getMethod(setter, value.getClass());
            method.invoke(target, value);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            setPropertyValueByField(property, value, target.getClass(), target);
        }
    }

    private static void setPropertyValueByField(String property, Object value, Class<?> classType, Object target) {
        try {
            Field field = classType.getDeclaredField(property);
            boolean isAccessible = field.canAccess(target);
            field.setAccessible(true);
            try {
                field.set(target, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                field.setAccessible(isAccessible);
            }
        } catch (NoSuchFieldException nsf) {
            if (classType.getSuperclass() != Object.class) {
                setPropertyValueByField(property, value, classType.getSuperclass(), target);
            }
            throw new RuntimeException(String.format("No field matching property '%s' was found", property), nsf);
        }
    }
}
