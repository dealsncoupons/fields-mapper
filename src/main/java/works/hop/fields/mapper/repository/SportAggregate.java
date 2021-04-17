package works.hop.fields.mapper.repository;

import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.CrudRepository;
import works.hop.fields.mapper.entity.Sport;

import java.util.UUID;

@Table("tbl_sport")
public interface SportAggregate extends CrudRepository<Sport, UUID> {
}
