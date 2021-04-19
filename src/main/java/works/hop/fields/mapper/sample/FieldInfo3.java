package works.hop.fields.mapper.sample;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Function;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldInfo3<A, B> {

    String source;
    String target;
    Function<A, B> resolver;

    public FieldInfo3(String source, String target) {
        this(source, target, null);
    }
}