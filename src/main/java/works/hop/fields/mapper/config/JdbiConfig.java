package works.hop.fields.mapper.config;

import org.jdbi.v3.core.Jdbi;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class JdbiConfig {

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public Jdbi jdbi(DataSource dataSource) {
        return Jdbi.create(dataSource);
    }
}
