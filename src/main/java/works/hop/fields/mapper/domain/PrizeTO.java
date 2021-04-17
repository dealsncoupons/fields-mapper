package works.hop.fields.mapper.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
public class PrizeTO {

    UUID id;
    int rank;
    String title;
    BigDecimal value;
    String currency;
    SportTO sport;
    PlayerTO winner;
    SponsorTO sponsor;
    Date dateAwarded;
    Date dateCreated;
}
