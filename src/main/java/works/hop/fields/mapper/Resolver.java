package works.hop.fields.mapper;

public interface Resolver<T, R> {

    R apply(T o);
}