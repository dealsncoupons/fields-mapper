package works.hop.fields.mapper.sample;

import com.google.common.primitives.Primitives;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public class FieldMapperTest {

    static final Logger log = LoggerFactory.getLogger(FieldMapperTest.class);

    public static void main_other(String[] args) {
        Map<String, Function<?, ?>> suppliers = new HashMap<>();
        suppliers.put("name", (context) -> "some name");
        suppliers.put("completed", (context) -> true);

        Resolver resolver = new Resolver() {
            @Override
            public <T, V> T resolve(Class<T> type, String key, V context) {
                Function<V, T> provider = (Function<V, T>) suppliers.get(key);
                return resolver(type, provider, context);
            }
        };

        Item item = new Item();
        item.name = resolver.resolve(String.class, "name", item);
        item.completed = resolver.resolve(Boolean.class, "completed", item);
        log.info("item - {}", item);
    }

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

        Map<String, Function<Object, Object>> suppliers = FieldMapperUtils.valueSuppliers(item, item.getClass(), item.getClass().getName());
        log.info("checking out value suppliers %n{}", suppliers);

//        Item clone = map(item, Item.class, new FieldMapper());
//        log.info("{}", clone);

        FieldMapper clone4Mapper = new FieldMapper();
        clone4Mapper.map(Item.class, ItemTO4.class);
        clone4Mapper.map("name", "task");
        clone4Mapper.map("completed", "done");
        clone4Mapper.map("notes", "notes", (Function<String, List<String>>) s -> Arrays.asList(s.split(",")));
        clone4Mapper.map("nested", "children");
//        ItemTO4 clone4 = map(item, ItemTO4.class, clone4Mapper);
//        log.info("{}", clone4);

        FieldMapper clone5Mapper = new FieldMapper();
        clone5Mapper.map("name", "item.name");
        clone5Mapper.map("completed", "item.completed");
        clone5Mapper.map(Item.class, Item.class, "item");
//        ItemTO5 clone5 = map(item, ItemTO5.class, clone5Mapper);
//        log.info("{}", clone5);
    }

    private static <T> T map(Object source, Class<T> target, FieldMapper fieldMapper) {
        T targetObj = FieldMapperUtils.newInstance(target);
        Map<Class<?>, List<Field>> fields = new HashMap<>();
        FieldMapperUtils.instanceFields(source.getClass(), fields);
        fields.forEach((clazz, fieldList) -> {
            fieldList.forEach(field -> {
                int fieldModifiers = field.getModifiers();
                Class<?> fieldType = field.getType();
                if (!Modifier.isStatic(fieldModifiers)) {
                    if ((fieldType.isPrimitive() || Primitives.isWrapperType(fieldType) || String.class.isAssignableFrom(fieldType))) {
                        mapBasicValue(field, source, targetObj, fieldMapper);
                    } else if (List.class.isAssignableFrom(fieldType)) {
                        mapListValue(field, source, targetObj, fieldMapper);
                    } else if (Set.class.isAssignableFrom(fieldType)) {
                        mapSetValue(field, source, targetObj, fieldMapper);
                    } else if (Map.class.isAssignableFrom(fieldType)) {
                        mapMapValue(field, source, targetObj, fieldMapper);
                    } else {
                        log.warn("field {} not mapped", field.getName());
                    }
                } else {
                    log.warn("ignoring static field - {}", field.getName());
                }
            });
        });
        return targetObj;
    }

    private static void mapBasicValue(Field field, Object source, Object target, FieldMapper fieldMapper) {
        String sourceProperty = field.getName();
        String targetProperty = fieldMapper.targetProperty(sourceProperty).orElse(sourceProperty);
        Field targetField = FieldMapperUtils.instanceField(target.getClass(), targetProperty);
        if (targetField != null) {
            Object sourceValue = FieldMapperUtils.getFieldValue(field, source);
            Object resolvedValue = Optional.ofNullable(fieldMapper.resolver(sourceProperty))
                    .map(resolver -> resolver.apply(sourceValue)).orElse(sourceValue);
            FieldMapperUtils.setFieldValue(targetField, target, resolvedValue);
        } else {
            log.warn("target field {} was not found", targetProperty);
        }
    }

    private static void mapListValue(Field field, Object source, Object target, FieldMapper fieldMapper) {
        String sourceProperty = field.getName();
        String targetProperty = fieldMapper.targetProperty(sourceProperty).orElse(sourceProperty);
        Field targetField = FieldMapperUtils.instanceField(target.getClass(), targetProperty);
        if (targetField != null) {
            List<Object> targetList = new ArrayList<>();
            List<?> sourceList = (List) FieldMapperUtils.getFieldValue(field, source);
            if (sourceList != null) {
                sourceList.forEach(listItem -> {
                    Class<?> sourceListItemType = listItem.getClass();
                    if (sourceListItemType.isPrimitive() || Primitives.isWrapperType(sourceListItemType) || String.class.isAssignableFrom(sourceListItemType)) {
                        targetList.add(listItem);
                    } else {
                        Class<?> targetListItemType = fieldMapper.targetClass(listItem.getClass()).orElse(listItem.getClass());
                        Object targetListItemValue = map(listItem, fieldMapper.targetClass(targetListItemType).orElse(targetListItemType), fieldMapper);
                        targetList.add(targetListItemValue);
                    }
                });
            }
            FieldMapperUtils.setFieldValue(targetField, target, targetList);
        } else {
            log.warn("target field {} was not found", targetProperty);
        }
    }

    private static void mapSetValue(Field field, Object source, Object target, FieldMapper fieldMapper) {
        String sourceProperty = field.getName();
        String targetProperty = fieldMapper.targetProperty(sourceProperty).orElse(sourceProperty);
        Field targetField = FieldMapperUtils.instanceField(target.getClass(), targetProperty);
        if (targetField != null) {
            Set<Object> targetSet = new HashSet<>();
            Set<?> sourceSet = (Set) FieldMapperUtils.getFieldValue(field, source);
            if (sourceSet != null) {
                sourceSet.forEach(setItem -> {
                    Class<?> sourceSetItemType = setItem.getClass();
                    if (sourceSetItemType.isPrimitive() || Primitives.isWrapperType(sourceSetItemType) || String.class.isAssignableFrom(sourceSetItemType)) {
                        targetSet.add(setItem);
                    } else {
                        Class<?> targetSetItemType = fieldMapper.targetClass(setItem.getClass()).orElse(setItem.getClass());
                        Object targetSetItemValue = map(setItem, fieldMapper.targetClass(targetSetItemType).orElse(targetSetItemType), fieldMapper);
                        targetSet.add(targetSetItemValue);
                    }
                });
            }
            FieldMapperUtils.setFieldValue(targetField, target, targetSet);
        } else {
            log.warn("target field {} was not found", targetProperty);
        }
    }

    private static void mapMapValue(Field field, Object source, Object target, FieldMapper fieldMapper) {
        String sourceProperty = field.getName();
        String targetProperty = fieldMapper.targetProperty(sourceProperty).orElse(sourceProperty);
        Field targetField = FieldMapperUtils.instanceField(target.getClass(), targetProperty);
        if (targetField != null) {
            Map<Object, Object> targetMap = new HashMap<>();
            Map<?, ?> sourceMap = (Map) FieldMapperUtils.getFieldValue(field, source);
            if (sourceMap != null) {
                sourceMap.forEach((mapKey, mapValue) -> {
                    Class<?> mapKeyType = mapKey.getClass();
                    Object targetMapKey = (mapKeyType.isPrimitive() || Primitives.isWrapperType(mapKeyType) || String.class.isAssignableFrom(mapKeyType)) ?
                            mapKey : map(mapKey, fieldMapper.targetClass(mapKeyType).orElse(mapKeyType), fieldMapper);

                    Class<?> mapValueType = mapValue.getClass();
                    Object targetMapValue = (mapValueType.isPrimitive() || Primitives.isWrapperType(mapValueType) || String.class.isAssignableFrom(mapValueType)) ?
                            mapKey : map(mapValue, fieldMapper.targetClass(mapValueType).orElse(mapValueType), fieldMapper);

                    targetMap.put(targetMapKey, targetMapValue);
                });
            }
            FieldMapperUtils.setFieldValue(targetField, target, targetMap);
        } else {
            log.warn("target field {} was not found", targetProperty);
        }
    }

    interface Resolver {
        default <T, V> T resolver(Class<T> type, Function<V, T> supplier, V context) {
            return type.cast(supplier.apply(context));
        }

        <T, V> T resolve(Class<T> type, String key, V context);
    }
}
