package demo.catalogue;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public final class CatalogueItemService {

    private final CatalogueItemRepository repository;

    public CatalogueItemService(final CatalogueItemRepository repository) {
        this.repository = requireNonNull(repository, "Repository cannot be null");
    }

    public Optional<CatalogueItemEntity> findById(final long id) {
        return repository.findById(id);
    }

    public List<CatalogueItemEntity> findAllById(final Set<Long> ids) {
        requireNonNull(ids, "IDs cannot be null");
        return repository.findAllById(ids);
    }

    public CatalogueItemEntity add(final CatalogueItemEntity entity) {
        requireNonNull(entity, "Entity cannot be null");
        return repository.save(entity);
    }

    public Optional<CatalogueItemEntity> findByGuid(UUID id) {
        requireNonNull(id, "ID cannot be null");
        return repository.findByGuid(id);
    }
}
