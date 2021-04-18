package works.hop.fields.mapper.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.AllArgsConstructor;
import ma.glasnost.orika.MapperFactory;
import org.springframework.stereotype.Component;
import works.hop.fields.mapper.domain.PlayerTO;
import works.hop.fields.mapper.entity.Player;
import works.hop.fields.mapper.repository.PlayerAggregate;

import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class PlayerFetcher implements DataFetcher<PlayerTO> {

    final PlayerAggregate playerAggregate;
    final MapperFactory mapperFactory;

    @Override
    public PlayerTO get(DataFetchingEnvironment environment) throws Exception {
        UUID playerId = environment.getArgument("player");
        Optional<Player> user = playerAggregate.findById(playerId);
        return user.map(appUser -> mapperFactory.getMapperFacade().map(appUser, PlayerTO.class)).orElse(null);
    }
}