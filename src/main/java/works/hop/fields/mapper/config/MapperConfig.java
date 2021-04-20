package works.hop.fields.mapper.config;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import works.hop.fields.mapper.domain.*;
import works.hop.fields.mapper.entity.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                .byDefault().register();
        mapperFactory.classMap(Sponsor.class, SponsorTO.class).byDefault().register();
        mapperFactory.classMap(Team.class, TeamTO.class).byDefault().register();
        mapperFactory.classMap(Sport.class, SportTO.class).byDefault().register();
        return mapperFactory;
    }

//    @Bean
    public DebeziumEngine<ChangeEvent<String, String>> debeziumEngine(Environment env) {
        final Properties props = new Properties();
        props.setProperty("name", env.getProperty("dbz.name"));
        props.setProperty("offset.storage", env.getProperty("offset.storage"));
        props.setProperty("offset.storage.file.filename", env.getProperty("offset.storage.file.filename"));
        props.setProperty("offset.flush.interval.ms", env.getProperty("offset.flush.interval.ms"));
        /* begin connector properties */
        props.setProperty("database.hostname", env.getProperty("database.hostname"));
        props.setProperty("database.port", env.getProperty("database.port"));
        props.setProperty("database.user", env.getProperty("database.user"));
        props.setProperty("database.password", env.getProperty("database.password"));
        props.setProperty("database.server.id", env.getProperty("database.server.id"));
        props.setProperty("database.server.name", env.getProperty("database.server.name"));
        props.setProperty("database.history", env.getProperty("database.history"));
        props.setProperty("database.history.file.filename", env.getProperty("database.history.file.filename"));

        return DebeziumEngine.create(Json.class)
                .using(props)
                .notifying(System.out::println).build();
    }

//    @PostConstruct
    public void init(DebeziumEngine<ChangeEvent<String, String>> engine) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(engine);
    }

//    @PreDestroy
    public void destroy(DebeziumEngine<ChangeEvent<String, String>> engine) throws IOException {
        engine.close();
    }
}
