package demo.catalogue;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Service
public final class CatalogueItemService {

    private final CatalogueItemRepository repository;

    public CatalogueItemService(final CatalogueItemRepository repository) {
        this.repository = requireNonNull(repository, "Repository cannot be null");
    }

    public Optional<CatalogueItemEntity> findByGuid(UUID guid) {
        requireNonNull(guid, "Guid cannot be null");
        return repository.findByGuid(guid);
    }
}
