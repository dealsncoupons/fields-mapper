package works.hop.fields.mapper.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("tbl_team")
@Data
public class Team {

    @Id
    UUID id;
    String name;
}
