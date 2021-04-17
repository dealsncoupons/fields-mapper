package works.hop.fields.mapper.sample;

public interface Resolver<T, R> {

    R apply(T o);
}