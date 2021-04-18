package works.hop.fields.mapper.config;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import works.hop.fields.mapper.domain.*;
import works.hop.fields.mapper.entity.*;

@Configuration
public class MapperConfig {

    @Bean
    MapperFactory mapperFactory() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        mapperFactory.classMap(Player.class, PlayerTO.class).byDefault().register();
        mapperFactory.classMap(Prize.class, PrizeTO.class)
                .field("sportId", "sport.id")
                .field("winnerId", "winner.id")
                .field("sponsorId", "sponsor.id")
                .byDefault().register();;
        mapperFactory.classMap(Sponsor.class, SponsorTO.class).byDefault().register();;
        mapperFactory.classMap(Team.class, TeamTO.class).byDefault().register();;
        mapperFactory.classMap(Sport.class, SportTO.class).byDefault().register();;
        return mapperFactory;
    }
}
