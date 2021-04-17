package works.hop.fields.mapper.sample;

import com.google.common.primitives.Primitives;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MappingInfo {

    final MapAtoB mapAtoB = new MapAtoB();
    final char[] delimiters = new char[]{'[', '.'};
    final Pattern mapAccessorPattern = Pattern.compile("([\\w_$]+)\\[([\\w_$]+?)\\]");

    Function<Object, Object> resolver;
    String source;
    String target;
    Object sourceObj;
    Class<?> targetCls;
    Class<?> paramType;
    Object targetObj;
    FieldMapper fieldMapper;

    public MappingInfo(String source, String target) {
        this.source = source;
        this.target = target;
    }

    public MappingInfo(String source, Object sourceObj, String target, Class<?> targetCls) {
        this.source = source;
        this.target = target;
        this.sourceObj = sourceObj;
        this.targetCls = targetCls;
    }

    public <T, R> MappingInfo(String source, String target, Function<T, R> resolver) {
        this(source, target);
        this.resolver = (Function<Object, Object>) resolver;
    }

    private String getterMethod(String fieldName) {
        return String.format("get%s%s", Character.toUpperCase(fieldName.charAt(0)), fieldName.substring(1));
    }

    private String setterMethod(String fieldName) {
        return String.format("set%s%s", Character.toUpperCase(fieldName.charAt(0)), fieldName.substring(1));
    }

    public void mapSupplier() {
        try {
            mapSupplierByMethod();
        } catch (NoSuchMethodException e) {
            mapSupplierByField();
        }
    }

    private void mapSupplierByMethod() throws NoSuchMethodException {
        String getter = getterMethod(source);
        Method method = sourceObj.getClass().getMethod(getter);
        int modifiers = method.getModifiers();
        if (!Modifier.isStatic(modifiers) && method.getReturnType() != void.class && method.getParameterCount() == 0) {
            paramType = method.getReturnType();
            mapAtoB.supplier = () -> {
                try {
                    return method.invoke(sourceObj);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    return null;
                }
            };
        }
    }

    private void mapSupplierByField() {
        Field field = findField(source, targetCls);
        int modifiers = field.getModifiers();
        if (!Modifier.isStatic(modifiers)) {
            paramType = field.getType();
            mapAtoB.supplier = () -> {
                boolean accessible = field.canAccess(sourceObj);
                field.setAccessible(true);
                try {
                    return field.get(sourceObj);
                } catch (IllegalAccessException iae) {
                    iae.printStackTrace();
                    return null;
                } finally {
                    field.setAccessible(accessible);
                }
            };
        }
    }

    private Field findField(String source, Class<?> targetCls) {
        try {
            return targetCls.getDeclaredField(source);
        } catch (NoSuchFieldException nsf) {
            if (targetCls.getSuperclass() != Object.class) {
                return findField(source, targetCls.getSuperclass());
            }
            throw new RuntimeException(String.format("No field matching property '%s' was found", source), nsf);
        }
    }

    public void mapConsumer() {
        mapConsumerProperty(target);
    }

    private void mapConsumerProperty(String property) {
        if (property.matches("^[\\w_$]+$")) {
            try {
                mapConsumerByMethod(property);
            } catch (NoSuchMethodException e) {
                mapConsumerByField(property);
            }
        } else {
            int delimiterIndex = findFirstMatch(property, delimiters);
            if (delimiterIndex != -1) {
                if (property.charAt(delimiterIndex) == '[') {
                    //must be a map property
                    Matcher matcher = mapAccessorPattern.matcher(property);
                    if (matcher.find()) {
                        String mapFieldName = matcher.group(1);
                        Object mapKey = matcher.group(2);
                        Map<Object, Object> mapValue = (Map<Object, Object>) getPropertyValueOrCreateNew(mapFieldName);
                        targetObj = mapValue.get(mapKey);
                        targetCls = targetObj.getClass();
                        mapConsumerProperty(property.substring(property.indexOf("]" + 1)));
                    }
                } else if (property.charAt(delimiterIndex) == '.') {
                    //must be nested property
                    String parentFieldName = property.substring(0, delimiterIndex);
                    Object parentFieldValue = getPropertyValueOrCreateNew(parentFieldName);
                    setPropertyValue(parentFieldName, parentFieldValue, targetObj);
                    targetObj = parentFieldValue;
                    targetCls = targetObj.getClass();
                    mapConsumerProperty(property.substring(delimiterIndex + 1));
                }
            } else {
                throw new RuntimeException(String.format("%s - %s", "Unexpected field definition", property));
            }
        }
    }

    private int findFirstMatch(String property, char[] targets) {
        for (int i = 0; i < property.length(); i++) {
            for (char ch : targets) {
                if (ch == property.charAt(i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void setPropertyValue(String property, Object value, Object target) {
        setPropertyValueByMethod(property, value, target);
    }

    private void setPropertyValueByMethod(String property, Object value, Object target) {
        String setter = setterMethod(property);
        try {
            Method method = target.getClass().getMethod(setter, value.getClass());
            method.invoke(target, value);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            setPropertyValueByField(property, value, target.getClass(), target);
        }
    }

    private void setPropertyValueByField(String property, Object value, Class<?> classType, Object target) {
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

    public Object getPropertyValue(String property) {
        try {
            return getPropertyValueByMethod(property);
        } catch (RuntimeException e) {
            return getPropertyValueByField(property, targetCls);
        }
    }

    public Object getPropertyValueOrCreateNew(String property) {
        return getPropertyValueOrCreateNewByMethod(property);
    }

    private Object getPropertyValueByMethod(String property) {
        String getter = getterMethod(property);
        try {
            Method method = targetCls.getMethod(getter);
            return method.invoke(targetObj);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return getPropertyValueByField(property, targetCls);
        }
    }

    private Object getPropertyValueOrCreateNewByMethod(String property) {
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
            return getPropertyValueOrCreateNewByField(property, targetCls);
        }
    }

    private Object getPropertyValueByField(String property, Class<?> classType) {
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
                return getPropertyValueByField(property, classType.getSuperclass());
            }
            throw new RuntimeException(String.format("No field matching property '%s' was found", property), nsf);
        }
    }

    private Object getPropertyValueOrCreateNewByField(String property, Class<?> classType) {
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
                return getPropertyValueOrCreateNewByField(property, classType.getSuperclass());
            }
            throw new RuntimeException(String.format("No field matching property '%s' was found", property), nsf);
        }
    }

    private void mapConsumerByMethod(String property) throws NoSuchMethodException {
        String setter = setterMethod(property);
        Method method = targetCls.getMethod(setter, paramType);
        int modifiers = method.getModifiers();
        if (!Modifier.isStatic(modifiers) && method.getReturnType() == void.class && method.getParameterCount() == 1) {
            mapAtoB.consumer = (value) -> {
                try {
                    Object targetValue = resolveValueType(fieldMapper, targetObj.getClass(), value);
                    method.invoke(targetObj, targetValue);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            };
        }
    }

    private void mapConsumerByField(String property) {
        Field field = findField(property, targetCls);
        int modifiers = field.getModifiers();
        if (!Modifier.isStatic(modifiers)) {
            mapAtoB.consumer = (value) -> {
                boolean accessible = field.canAccess(targetObj);
                field.setAccessible(true);
                try {
                    Object targetValue = resolveValueType(fieldMapper, targetObj.getClass(), value);
                    field.set(targetObj, targetValue);
                } catch (IllegalAccessException iae) {
                    iae.printStackTrace();
                } finally {
                    field.setAccessible(accessible);
                }
            };
        }
    }

    private Object resolveValueType(FieldMapper mapper, Class<?> expectedType, Object sourceValue) {
        if (sourceValue.getClass().isPrimitive() || Primitives.isWrapperType(sourceValue.getClass()) || String.class.isAssignableFrom(sourceValue.getClass())) {
            return sourceValue;
        } else if (List.class.isAssignableFrom(sourceValue.getClass())) {
            List<?> list = (List<?>) sourceValue;
            return list.stream().map(value -> resolveValueType(mapper, expectedType, value)).collect(Collectors.toList());
        } else if (Set.class.isAssignableFrom(sourceValue.getClass())) {
            Set<?> set = (Set<?>) sourceValue;
            return set.stream().map(value -> resolveValueType(mapper, expectedType, value)).collect(Collectors.toSet());
        } else if (Map.class.isAssignableFrom(sourceValue.getClass())) {
            Map<?, ?> map = (Map<?, ?>) sourceValue;
            return map.entrySet().stream().collect(
                    Collectors.toMap(
                            e -> resolveValueType(mapper, e.getKey().getClass(), e.getKey()),
                            e -> resolveValueType(mapper, e.getValue().getClass(), e.getValue())
                    )
            );
        } else {
            return mapper.map(sourceValue, expectedType);
        }
    }

    static class MapAtoB {
        Supplier<Object> supplier;
        Consumer<Object> consumer;
    }
}