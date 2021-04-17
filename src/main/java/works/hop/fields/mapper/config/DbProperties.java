package works.hop.fields.mapper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "db")
@Data
public class DbProperties {

    String url;
    String username;
    String password;
}
