package works.hop.fields.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    String name;
    Boolean completed;
    String notes;
    List<Item> subList;
}
