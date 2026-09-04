package demo.catalogue;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;

public interface CatalogueItemRepository extends ListCrudRepository<CatalogueItemEntity, Long> {

    Optional<CatalogueItemEntity> findByGuid(UUID id);
}
