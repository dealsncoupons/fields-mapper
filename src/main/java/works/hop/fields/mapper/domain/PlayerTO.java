package works.hop.fields.mapper.domain;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;
import works.hop.fields.mapper.entity.Team;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class PlayerTO {

    UUID id;
    String firstName;
    String lastName;
    Date birthDate;
    String homeCity;
    String homeCountry;
    List<Team> teams;
}
