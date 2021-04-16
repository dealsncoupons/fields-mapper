package works.hop.fields.mapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemTO3 {

    String task;
    Boolean done;
    List<String> notes;
}
