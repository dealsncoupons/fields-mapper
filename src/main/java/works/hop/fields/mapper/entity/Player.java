package works.hop.fields.mapper.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;
import java.util.UUID;

@Table("tbl_player")
@Data
public class Player {

    @Id
    UUID id;
    String firstName;
    String lastName;
    @Column("dob")
    Date birthDate;
    String homeCity;
    String homeCountry;
    UUID teamId;
}
