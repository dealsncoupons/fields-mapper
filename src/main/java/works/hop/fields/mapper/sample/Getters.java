package works.hop.fields.mapper.sample;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Getters {

    public static String getterMethod(String fieldName) {
        return String.format("get%s%s", Character.toUpperCase(fieldName.charAt(0)), fieldName.substring(1));
    }

    public static Object getPropertyValue(String property, Class<?> targetCls, Object targetObj) {
        try {
            return getPropertyValueByMethod(property, targetCls, targetObj);
        } catch (RuntimeException e) {
            return getPropertyValueByField(property, targetCls, targetObj);
        }
    }

    public static Object getPropertyValueOrCreateNew(String property, Class<?> targetCls, Object targetObj) {
        return getPropertyValueOrCreateNewByMethod(property, targetCls, targetObj);
    }

    private static Object getPropertyValueByMethod(String property, Class<?> targetCls, Object targetObj) {
        String getter = getterMethod(property);
        try {
            Method method = targetCls.getMethod(getter);
            return method.invoke(targetObj);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return getPropertyValueByField(property, targetCls, targetObj);
        }
    }

    private static Object getPropertyValueOrCreateNewByMethod(String property, Class<?> targetCls, Object targetObj) {
        String getter = getterMethod(property);
        try {
            Method method = targetCls.getMethod(getter);
            Object propertyValue = method.invoke(targetObj);
            if (propertyValue == null) {
                return method.getReturnType().getConstructor().newInstance();
            }
            return propertyValue;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
            return getPropertyValueOrCreateNewByField(property, targetCls, targetObj);
        }
    }

    private static Object getPropertyValueByField(String property, Class<?> classType, Object targetObj) {
        try {
            Field field = classType.getDeclaredField(property);
            boolean isAccessible = field.canAccess(targetObj);
            field.setAccessible(true);
            try {
                return field.get(targetObj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            } finally {
                field.setAccessible(isAccessible);
            }
        } catch (NoSuchFieldException nsf) {
            if (classType.getSuperclass() != Object.class) {
                return getPropertyValueByField(property, classType.getSuperclass(), targetObj);
            }
            throw new RuntimeException(String.format("No field matching property '%s' was found", property), nsf);
        }
    }

    private static Object getPropertyValueOrCreateNewByField(String property, Class<?> classType, Object targetObj) {
        try {
            Field field = classType.getDeclaredField(property);
            boolean isAccessible = field.canAccess(targetObj);
            field.setAccessible(true);
            try {
                Object propertyValue = field.get(targetObj);
                if (propertyValue == null) {
                    return field.getType().getConstructor().newInstance();
                }
                return propertyValue;
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                e.printStackTrace();
                return null;
            } finally {
                field.setAccessible(isAccessible);
            }
        } catch (NoSuchFieldException nsf) {
            if (classType.getSuperclass() != Object.class) {
                return getPropertyValueOrCreateNewByField(property, classType.getSuperclass(), targetObj);
            }
            throw new RuntimeException(String.format("No field matching property '%s' was found", property), nsf);
        }
    }
}
