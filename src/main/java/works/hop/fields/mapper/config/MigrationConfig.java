package works.hop.fields.mapper.config;

//import org.flywaydb.core.Flyway;
import org.flywaydb.core.Flyway;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties(DbProperties.class)
@PropertySource("classpath:application-local.properties")
public class MigrationConfig {

    @Bean
    Flyway flyway(DbProperties properties) {
        Flyway flyway = Flyway.configure()
                .dataSource(properties.url, properties.username, properties.password)
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
        return flyway;
    }
}
