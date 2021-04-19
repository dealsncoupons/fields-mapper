package works.hop.fields.mapper.sample;

import com.google.common.primitives.Primitives;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public class FieldMapperTest2 {

    public static void main(String[] args) {
        int whichOne = args.length > 0 ? Integer.parseInt(args[0]) : 0;
        if (whichOne == 0) {
            useLevels();
        } else {
            useItems();
        }
    }

    private static void useLevels() {
        Level2 level = new Level2();
        level.setId(100L);
        level.setName("level3");
        level.setTitle("happy coding");
        level.setNumbers(Arrays.asList(1, 2, 3));
        level.setLevel0s(Arrays.asList(
                new Level0(1L, "one"),
                new Level0(2L, "two"),
                new Level0(3L, "three")));

        Level2 clone = mapTargetField(new FieldInfoMap(), level, Level2.class);
        System.out.println(clone);
    }

    private static void useItems() {
        Item item = new Item();
        item.setName("name");
        item.setCompleted(false);
        item.setNotes("one,two,three");
        item.setItems(Arrays.asList("four", "five", "six"));
        Item child1 = new Item("child1", false, "c_one,c_two,c_three", Arrays.asList("C-four", "C-five", "C-six"), emptyList(), emptyMap());
        Item child2 = new Item("child2", true, "c_eleven,c_twelve,c_thirteen", Arrays.asList("D-four", "D-five", "D-six"), emptyList(), emptyMap());
        Item child3 = new Item("child3", true, "c_twenty_one,c_twenty_two,c_twenty_three", Arrays.asList("E-four", "E-five", "E-six"), emptyList(), emptyMap());
        item.setNested(Arrays.asList(child1, child2, child3));
        Map<String, Item> grouped = new HashMap<>();
        grouped.put("child1", child1);
        grouped.put("child2", child2);
        grouped.put("child3", child3);
        item.setGroups(grouped);

        Map<Class<?>, List<Field>> fields = instanceFields(item.getClass());
        System.out.println(fields);

        Map<Object, Object> mapper = new HashMap<>();
        fieldsMap(item, mapper);
        System.out.println(mapper);

        FieldInfoMap fieldsMap = new FieldInfoMap();
        fieldsMap.map("name", "task");
        fieldsMap.map("completed", "done");
        fieldsMap.map("notes", "notes", (Function<String, List<String>>) s -> Arrays.asList(s.split(",")));
        fieldsMap.map("items", "list");
        fieldsMap.map("nested", "children");

        ItemTO4 itemTO = mapTargetField(fieldsMap, item, ItemTO4.class);
//        ItemTO4 itemTO = mapTo(item, mapper, fieldsMap, ItemTO4.class);
        System.out.println(itemTO);
    }

//    private static <T> T mapTo(Object sourceObj, Map<Object, Object> sourceFields, Class<T> targetType) {
//        return mapTo(sourceObj, sourceFields, new FieldInfoMap(), targetType);
//    }
//
//    private static <T> T mapTo(Object sourceObj, Map<Object, Object> sourceFields, FieldInfoMap fieldsMap, Class<T> targetType) {
//        T targetObj = createNewInstance(targetType);
//        mapSourceFields(fieldsMap, sourceFields, sourceObj, targetObj);
//        return targetObj;
//    }

    private static <T> T createNewInstance(Class<T> targetType) {
        try {
            return targetType.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not create instance of class provided", e);
        }
    }

    private static <T> void mapSourceFields(FieldInfoMap fieldsMap, Map<Object, Object> sourceFields, Object sourceObj, T targetObj) {
        sourceFields.forEach((key, value) -> {
            String keyValue = key.toString();
            FieldInfo sourceFieldInfo = fieldsMap.find(keyValue).orElse(new FieldInfo(keyValue, keyValue));
            Field sourceField = instanceField(sourceObj.getClass(), sourceFieldInfo.source);
            if (sourceField != null) {
                if (TriConsumer.class.isAssignableFrom(value.getClass())) {
                    acceptValue(sourceField, sourceFieldInfo, sourceObj, targetObj, value);
                } else if (Map.class.isAssignableFrom(value.getClass())) {
                    Map<?, ?> consumersMap = (Map<?, ?>) value;
                    consumersMap.forEach((mapKey, mapValue) -> {
                        if (mapValue != null) {
                            mapSourceFields(fieldsMap, sourceFields, sourceObj, mapValue);
                        }
                    });
                    System.out.println("traversed list consumers list");
                } else {
                    System.out.printf("%s type Not yeh handled%n", value.getClass());
                }
            }
        });
    }

    private static void acceptValue(Field sourceField, FieldInfo sourceFieldInfo, Object sourceObj, Object targetObj, Object value) {
        TriConsumer consumer = (TriConsumer) value;
        Field targetField = instanceField(targetObj.getClass(), sourceFieldInfo.target);
        if (targetField != null) {
            Object sourceValue = getFieldValue(sourceField, sourceObj);
            if (sourceFieldInfo.resolver != null) {
                consumer.accept(targetField, targetObj, sourceFieldInfo.resolver.apply(sourceValue));
            } else {
                consumer.accept(targetField, targetObj, sourceValue);
            }
            System.out.printf("%s value was accepted", targetField.getName());
        }
    }

    private static void fieldsMap(Object source, Map<Object, Object> mapper) {
        Map<Class<?>, List<Field>> fields = instanceFields(source.getClass());
        fields.forEach((clazz, list) -> {
            list.forEach(field -> {
                if (field.getType().isPrimitive() || Primitives.isWrapperType(field.getType()) || String.class.isAssignableFrom(field.getType())) {
                    mapper.put(field.getName(), (TriConsumer) FieldMapperTest2::setFieldValue);
                } else if (List.class.isAssignableFrom(field.getType())) {
                    List<?> listValue = (List<?>) getFieldValue(field, source);
                    if (listValue != null) {
                        AtomicInteger index = new AtomicInteger(0);
                        listValue.forEach(value -> {
                            if (value.getClass().isPrimitive() || Primitives.isWrapperType(value.getClass()) || String.class.isAssignableFrom(value.getClass())) {
                                mapper.put(field.getName(), (TriConsumer) FieldMapperTest2::setFieldValue);
                            } else {
                                Map<Object, Object> nestedMapper = new HashMap<>();
                                fieldsMap(value, nestedMapper);
                                mapper.put(field.getName(), nestedMapper);
                            }
                        });
                    }
                } else if (Set.class.isAssignableFrom(field.getType())) {
                    Set<?> setValue = (Set<?>) getFieldValue(field, source);
                    if (setValue != null) {
                        AtomicInteger index = new AtomicInteger(0);
                        setValue.forEach(value -> {
                            if (value.getClass().isPrimitive() || Primitives.isWrapperType(value.getClass()) || String.class.isAssignableFrom(value.getClass())) {
                                mapper.put(field.getName(), (TriConsumer) FieldMapperTest2::setFieldValue);
                            } else {
                                Map<Object, Object> nestedMapper = new HashMap<>();
                                fieldsMap(value, nestedMapper);
                                mapper.put(field.getName(), nestedMapper);
                            }
                        });
                    }
                } else if (Map.class.isAssignableFrom(field.getType())) {
                    Map<Object, Object> mapValue = (Map<Object, Object>) getFieldValue(field, source);
                    MapEntry mapEntry = new MapEntry();
                    if (mapValue != null) {
                        mapValue.forEach((key, value) -> {
                            Map<Object, Object> keyValueFields = new HashMap<>();
                            if (key.getClass().isPrimitive() || Primitives.isWrapperType(key.getClass()) || String.class.isAssignableFrom(key.getClass())) {
                                keyValueFields.put(key, (TriConsumer) FieldMapperTest2::setFieldValue);
                            } else {
                                fieldsMap(key, keyValueFields);
                            }
                            mapEntry.key = keyValueFields;
                            Map<Object, Object> valValueFields = new HashMap<>();
                            if (value.getClass().isPrimitive() || Primitives.isWrapperType(value.getClass()) || String.class.isAssignableFrom(value.getClass())) {
                                valValueFields.put(value, (TriConsumer) FieldMapperTest2::setFieldValue);
                            } else {
                                fieldsMap(value, valValueFields);
                            }
                            mapEntry.value = valValueFields;
                        });
                        mapper.put(field.getName(), mapEntry);
                    }
                } else {
                    Object fieldValue = getFieldValue(field, source);
                    if (fieldValue != null) {
                        Object targetValue = mapTargetField(new FieldInfoMap(), fieldValue, field.getType());
                        mapper.put(field.getName(), new BiConsumer<Field, Object>() {

                            @Override
                            public void accept(Field field, Object target) {
                                setFieldValue(field, target, targetValue);
                            }
                        });
                    }
                }
            });
        });
    }

    private static <T> T mapTargetField(FieldInfoMap fieldsMap, Object fieldValue, Class<T> targetType) {
        Map<Object, Object> sourceFields = new HashMap<>();
        fieldsMap(fieldValue, sourceFields);

        T targetObj = createNewInstance(targetType);
        mapSourceFields(fieldsMap, sourceFields, fieldValue, targetObj);
        return targetObj;
    }

    private static Map<Class<?>, List<Field>> instanceFields(Class<?> type) {
        Map<Class<?>, List<Field>> fields = new HashMap<>();
        fields.put(type, Arrays.stream(type.getDeclaredFields()).filter(field ->
                !Modifier.isStatic(field.getModifiers())
        ).collect(Collectors.toList()));
        if (type.getSuperclass() != Object.class) {
            fields.putAll(instanceFields(type.getSuperclass()));
        }
        return fields;
    }

    private static Field instanceField(Class<?> type, String fieldName) {
        try {
            return type.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (type.getSuperclass() != Object.class) {
                return instanceField(type.getSuperclass(), fieldName);
            }
            return null;
        }
    }

    private static void setFieldValue(Field field, Object target, Object value) {
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

    private static Object getFieldValue(Field field, Object target) {
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

    interface TriConsumer {
        void accept(Field field, Object target, Object value);
    }

    @Data
    static class MapEntry {
        Object key;
        Object value;
    }

    @Data
    static class FieldInfoMap {
        Map<String, FieldInfo> mapping = new HashMap<>();

        public void map(String source, String target) {
            this.mapping.put(source, new FieldInfo(source, target));
        }

        public <T, R> void map(String source, String target, Function<T, R> resolver) {
            this.mapping.put(source, new FieldInfo(source, target, resolver));
        }

        public Optional<FieldInfo> find(String fieldName) {
            return Optional.ofNullable(mapping.get(fieldName));
        }
    }
}
