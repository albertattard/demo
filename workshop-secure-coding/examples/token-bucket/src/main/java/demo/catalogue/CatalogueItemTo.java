package demo.catalogue;

import static java.util.Objects.requireNonNull;

public record CatalogueItemTo(long id, String caption, String description) {

    public static CatalogueItemTo of(final CatalogueItemEntity entity) {
        requireNonNull(entity, "Entity cannot be null");
        return new CatalogueItemTo(entity.id(), entity.caption(), entity.description());
    }
}
