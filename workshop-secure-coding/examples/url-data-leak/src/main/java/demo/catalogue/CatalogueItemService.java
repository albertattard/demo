package demo.catalogue;

import org.springframework.stereotype.Service;

import java.util.Optional;

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
}
