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

import static works.hop.fields.mapper.sample.Getters.getPropertyValueOrCreateNew;
import static works.hop.fields.mapper.sample.Getters.getterMethod;
import static works.hop.fields.mapper.sample.Setters.setPropertyValue;
import static works.hop.fields.mapper.sample.Setters.setterMethod;

public class FieldInfo {

    final static char[] delimiters = new char[]{'[', '.'};
    final static Pattern mapAccessorPattern = Pattern.compile("([\\w_$]+)\\[([\\w_$]+?)\\]");

    final MapAtoB mapAtoB = new MapAtoB();

    String source;
    Object sourceObj;
    Class<?> paramType;
    String target;
    Class<?> targetCls;
    Object targetObj;
    FieldMapper fieldMapper;
    Function<Object, Object> resolver;

    public FieldInfo(String source, String target) {
        this.source = source;
        this.target = target;
    }

    public <T, R> FieldInfo(String source, String target, Function<T, R> resolver) {
        this(source, target);
        this.resolver = (Function<Object, Object>) resolver;
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
            int delimiterIndex = findFirstMatch(property);
            if (delimiterIndex != -1) {
                if (property.charAt(delimiterIndex) == '[') {
                    //must be a map property
                    Matcher matcher = mapAccessorPattern.matcher(property);
                    if (matcher.find()) {
                        String mapFieldName = matcher.group(1);
                        Object mapKey = matcher.group(2);
                        Map<Object, Object> mapValue = (Map<Object, Object>) getPropertyValueOrCreateNew(mapFieldName, targetCls, targetObj);
                        targetObj = mapValue.get(mapKey);
                        targetCls = targetObj.getClass();
                        mapConsumerProperty(property.substring(property.indexOf("]" + 1)));
                    } else {
                        throw new RuntimeException(String.format("%s - Expected expression to contain valid [] syntax", property));
                    }
                } else if (property.charAt(delimiterIndex) == '.') {
                    //must be nested property
                    String parentFieldName = property.substring(0, delimiterIndex);
                    Object parentFieldValue = getPropertyValueOrCreateNew(parentFieldName, targetCls, targetObj);
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

    private int findFirstMatch(String property) {
        for (int i = 0; i < property.length(); i++) {
            for (char ch : FieldInfo.delimiters) {
                if (ch == property.charAt(i)) {
                    return i;
                }
            }
        }
        return -1;
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