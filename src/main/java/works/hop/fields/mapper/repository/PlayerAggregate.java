package works.hop.fields.mapper.repository;

import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.CrudRepository;
import works.hop.fields.mapper.entity.Player;

import java.util.UUID;

@Table("tbL_player")
public interface PlayerAggregate extends CrudRepository<Player, UUID> {
}
