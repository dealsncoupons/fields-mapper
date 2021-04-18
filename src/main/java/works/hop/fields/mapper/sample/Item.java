package works.hop.fields.mapper.sample;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    String name;
    Boolean completed;
    String notes;
    List<String> items;
    List<Item> nested;
    Map<String, Item> groups;
}
