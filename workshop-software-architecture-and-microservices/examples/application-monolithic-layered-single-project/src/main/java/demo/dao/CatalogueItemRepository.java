package demo.dao;

import org.springframework.data.repository.ListCrudRepository;

public interface CatalogueItemRepository extends ListCrudRepository<CatalogueItemEntity, Long> {}
