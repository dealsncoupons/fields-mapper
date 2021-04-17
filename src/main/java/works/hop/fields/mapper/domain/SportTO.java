package works.hop.fields.mapper.domain;

import lombok.Data;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Data
public class SportTO {

    UUID id;
    String name;
    String venue;
    Date startTime;
    Set<TeamTO> teams;
}
