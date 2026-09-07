package demo.catalogue;

import java.util.UUID;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public record CatalogueItemTo(UUID guid, String caption, String description) {

    public static CatalogueItemTo of(final CatalogueItemEntity entity) {
        requireNonNull(entity, "Entity cannot be null");
        return new CatalogueItemTo(entity.guid(), entity.caption(), entity.description());
    }

    public <T> T map(final Function<CatalogueItemTo, T> mapper) {
        requireNonNull(mapper, "Mapper cannot be nul");
        return mapper.apply(this);
    }
}
