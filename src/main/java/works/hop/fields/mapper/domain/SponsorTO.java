package works.hop.fields.mapper.domain;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SponsorTO {

    UUID id;
    String name;
    String missionStatement;
    List<PrizeTO> prizes;
}
