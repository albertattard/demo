package demo.catalogue;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

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
}
