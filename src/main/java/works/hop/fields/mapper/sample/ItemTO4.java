package works.hop.fields.mapper.sample;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemTO4 {

    String task;
    Boolean done;
    List<String> notes;
    List<ItemTO4> children;
    Map<String, ItemTO4> groups;
}
