package works.hop.fields.mapper.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Table("tbl_prize")
@Data
public class Prize {

    @Id
    UUID id;
    int rank;
    String title;
    @Column("prize_value")
    BigDecimal value;
    @Column("value_currency")
    String currency;
    UUID sportId;
    UUID winnerId;
    UUID sponsorId;
    Date dateAwarded;
    Date dateCreated;
}
