package works.hop.fields.mapper.sample;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Function;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassInfo3<A, B> {

    Class<A> source;
    Class<B> target;
    String prefix;

    public ClassInfo3(Class<A> source, Class<B> target) {
        this(source, target, "");
    }
}