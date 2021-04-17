package works.hop.fields.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import works.hop.fields.mapper.repository.PlayerAggregate;
import works.hop.fields.mapper.repository.SportAggregate;

@SpringBootApplication
public class FieldsMapperApplication implements CommandLineRunner {

    @Autowired
    PlayerAggregate playerAggregate;
    @Autowired
    SportAggregate sportAggregate;

    public static void main(String[] args) {
        SpringApplication.run(FieldsMapperApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        playerAggregate.findAll().forEach(player -> {
            System.out.printf("player - %s, team - %s%n", player.getId(), player.getTeamId());
        });
        sportAggregate.findAll().forEach(sport -> {
            System.out.printf("sport - %s%n", sport.getId());
            sport.getTeams().forEach(team -> {
                System.out.printf("team - %s%n", team.getId());
            });
        });
    }
}
