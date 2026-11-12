package demo.catalogue;

import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Service
public final class CatalogueItemService {

    private final CatalogueItemRepository repository;
    private final CatalogueItemGateway gateway;

    public CatalogueItemService(final CatalogueItemRepository repository, final CatalogueItemGateway gateway) {
        this.repository = requireNonNull(repository, "Repository cannot be null");
        this.gateway = requireNonNull(gateway, "Gateway cannot be null");
    }

    public Optional<CatalogueItemEntity> findById(final long id) {
        return repository.findById(id);
    }

    public CatalogueItemEntity add(final CatalogueItemEntity entity) {
        requireNonNull(entity, "Entity cannot be null");
        final CatalogueItemEntity saved = repository.save(entity);
        gateway.newCatalogueItem(saved);
        return saved;
    }
}
