package works.hop.fields.mapper.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

class FieldMapperTest {

    static final Logger log = LoggerFactory.getLogger(FieldMapperTest.class);

    public static void main(String[] args) {
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

    interface Resolver {
        default <T, V> T resolver(Class<T> type, Function<V, T> supplier, V context) {
            return type.cast(supplier.apply(context));
        }

        <T, V> T resolve(Class<T> type, String key, V context);
    }
}