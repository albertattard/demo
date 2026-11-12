package demo.external.catalogue;

import org.springframework.data.repository.ListCrudRepository;

public interface CatalogueRepository extends ListCrudRepository<CatalogueItemEntity, Long> {}
