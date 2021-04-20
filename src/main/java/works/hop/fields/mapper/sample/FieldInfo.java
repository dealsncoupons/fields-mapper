package works.hop.fields.mapper.sample;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Function;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldInfo<A, B> {

    String source;
    String target;
    Function<A, B> resolver;

    public FieldInfo(String source, String target) {
        this(source, target, null);
    }
}