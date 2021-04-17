package works.hop.fields.mapper.repository;

import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.CrudRepository;
import works.hop.fields.mapper.entity.Sponsor;

import java.util.UUID;

@Table("tbl_sponsor")
public interface SponsorAggregate extends CrudRepository<Sponsor, UUID> {
}
