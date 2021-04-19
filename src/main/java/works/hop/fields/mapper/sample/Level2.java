package works.hop.fields.mapper.sample;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Level2 extends Level1 {

    List<Integer> numbers;
    List<Level0> level0s;
}