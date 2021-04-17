package works.hop.fields.mapper.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@Table("tbl_sponsor")
@Data
public class Sponsor {

    @Id
    UUID id;
    String name;
    String missionStatement;
    @Column("sponsor_id")
    List<Prize> prizes;
}
