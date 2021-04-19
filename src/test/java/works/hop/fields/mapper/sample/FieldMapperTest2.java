package works.hop.fields.mapper.sample;

import com.google.common.primitives.Primitives;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public class FieldMapperTest2 {

    public static void main(String[] args) {
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

        Map<Class<?>, List<Field>> fields = new HashMap<>();
        FieldMapperUtils.instanceFields(item.getClass(), fields);
        System.out.println(fields);

        Map<Object, Object> mapper = new HashMap<>();
        fieldsMap(item, mapper);
        System.out.println(mapper);

        ItemTO4 itemTO = mapTo(item, mapper, ItemTO4.class);
        System.out.println(itemTO);
    }

    private static <T> T mapTo(Object sourceObj, Map<Object, Object> sourceFields, Class<T> targetType) {
        FieldInfoMap fieldsMap = new FieldInfoMap();
        fieldsMap.map("name", "task");
        fieldsMap.map("completed", "done");
        fieldsMap.map("notes", "notes", (Function<String, List<String>>) s -> Arrays.asList(s.split(",")));

        Map<Class<?>, List<Field>> targetFields = new HashMap<>();
        FieldMapperUtils.instanceFields(targetType, targetFields);
        System.out.println(targetFields);

        T targetObj;
        try {
            targetObj = targetType.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not create instance of class provided", e);
        }

        sourceFields.forEach((key, value) -> {
            String keyValue = key.toString();
            Optional<FieldInfo> sourceFieldInfoOptional = fieldsMap.find(keyValue);
            if (sourceFieldInfoOptional.isPresent()) {
                FieldInfo sourceFieldInfo = sourceFieldInfoOptional.get();
                Field targetField = FieldMapperUtils.instanceField(targetType, sourceFieldInfo.target);
                if (targetField != null) {
                    if (TriConsumer.class.isAssignableFrom(value.getClass())) {
                        TriConsumer consumer = (TriConsumer) value;
                        Field sourceField = FieldMapperUtils.instanceField(sourceObj.getClass(), sourceFieldInfo.source);
                        if (sourceField != null) {
                            Object sourceValue = FieldMapperUtils.getFieldValue(sourceField, sourceObj);
                            if (sourceFieldInfo.resolver != null) {
                                consumer.accept(targetField, targetObj, sourceFieldInfo.resolver.apply(sourceValue));
                            } else {
                                consumer.accept(targetField, targetObj, sourceValue);
                            }
                            System.out.println("value set");
                        }
                    }
                }
            } else {
                System.out.printf("Field %s is not mapped%n", keyValue);
            }
        });
        return targetObj;
    }

    private static void fieldsMap(Object source, Map<Object, Object> mapper) {
        Map<Class<?>, List<Field>> fields = new HashMap<>();
        FieldMapperUtils.instanceFields(source.getClass(), fields);
        fields.forEach((cls, list) -> {
            list.forEach(field -> {
                if (field.getType().isPrimitive() || Primitives.isWrapperType(field.getType()) || String.class.isAssignableFrom(field.getType())) {
                    mapper.put(field.getName(), (TriConsumer) FieldMapperUtils::setFieldValue);
                } else if (List.class.isAssignableFrom(field.getType())) {
                    List<?> listValue = (List<?>) FieldMapperUtils.getFieldValue(field, source);
                    if (listValue != null) {
                        Map<Object, Object> valueFields = new HashMap<>();
                        listValue.forEach(value -> {
                            if (value.getClass().isPrimitive() || Primitives.isWrapperType(value.getClass()) || String.class.isAssignableFrom(value.getClass())) {
                                valueFields.put(field.getName(), (TriConsumer) FieldMapperUtils::setFieldValue);
                            } else {
                                fieldsMap(value, valueFields);
                            }
                        });
                        mapper.put(field.getName(), valueFields);
                    }
                } else if (Set.class.isAssignableFrom(field.getType())) {
                    Set<?> setValue = (Set<?>) FieldMapperUtils.getFieldValue(field, source);
                    if (setValue != null) {
                        Map<Object, Object> valueFields = new HashMap<>();
                        setValue.forEach(value -> {
                            if (value.getClass().isPrimitive() || Primitives.isWrapperType(value.getClass()) || String.class.isAssignableFrom(value.getClass())) {
                                valueFields.put(field.getName(), (TriConsumer) FieldMapperUtils::setFieldValue);
                            } else {
                                fieldsMap(value, valueFields);
                            }
                        });
                        mapper.put(field.getName(), valueFields);
                    }
                } else if (Map.class.isAssignableFrom(field.getType())) {
                    Map<Object, Object> mapValue = (Map<Object, Object>) FieldMapperUtils.getFieldValue(field, source);
                    MapEntry mapEntry = new MapEntry();
                    if (mapValue != null) {
                        mapValue.forEach((key, value) -> {
                            Map<Object, Object> keyValueFields = new HashMap<>();
                            if (key.getClass().isPrimitive() || Primitives.isWrapperType(key.getClass()) || String.class.isAssignableFrom(key.getClass())) {
                                keyValueFields.put(key, (TriConsumer) FieldMapperUtils::setFieldValue);
                            } else {
                                fieldsMap(key, keyValueFields);
                            }
                            mapEntry.key = keyValueFields;
                            Map<Object, Object> valValueFields = new HashMap<>();
                            if (value.getClass().isPrimitive() || Primitives.isWrapperType(value.getClass()) || String.class.isAssignableFrom(value.getClass())) {
                                valValueFields.put(value, (TriConsumer) FieldMapperUtils::setFieldValue);
                            } else {
                                fieldsMap(value, valValueFields);
                            }
                            mapEntry.value = valValueFields;
                        });
                        mapper.put(field.getName(), mapValue);
                    }
                } else {
                    throw new RuntimeException("Unhandled use case encountered");
                }
            });
        });
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
            return mapping.values().stream().filter(info -> info.target.equals(fieldName)).findFirst();
        }
    }
}
