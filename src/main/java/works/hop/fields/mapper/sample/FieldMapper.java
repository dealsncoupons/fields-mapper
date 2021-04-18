package works.hop.fields.mapper.sample;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FieldMapper {

    private final Map<String, FieldInfo> mapping = new HashMap<>();

    public void map(String source, String target) {
        this.mapping.put(source, new FieldInfo(source, target));
    }

    public <T, R> void map(String source, String target, Function<T, R> resolver) {
        this.mapping.put(source, new FieldInfo(source, target, resolver));
    }

    public <T> T map(Object source, Class<T> type) {
        T target = newInstance(type);
        mapping.forEach((key, value) -> {
            value.sourceObj = source;
            value.targetCls = type;
            value.targetObj = target;
            value.fieldMapper = this;
            value.mapSupplier();
            value.mapConsumer();
            Object supplierValue = value.mapAtoB.supplier.get();
            value.mapAtoB.consumer.accept(value.resolver != null ?
                    value.resolver.apply(supplierValue) :
                    supplierValue);
        });
        return target;
    }

    public <T> T newInstance(Class<T> target) {
        try {
            return target.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Expecting a no-args constructor to create the target object", e);
        }
    }
}
