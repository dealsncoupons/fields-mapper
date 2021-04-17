package works.hop.fields.mapper.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Table("tbl_sport")
@Data
public class Sport {

    @Id
    UUID id;
    String name;
    String venue;
    Date startTime;
    @Column("sport_id")
    Set<Team> teams;
}
