package demo.catalogue;

import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface CatalogueItemRepository extends ListCrudRepository<CatalogueItemEntity, Long> {

    Optional<CatalogueItemEntity> findByGuid(UUID guid);
}
