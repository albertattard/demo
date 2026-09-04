package demo.catalogue;

import static java.util.Objects.requireNonNull;

import java.util.UUID;
import java.util.function.Function;

public record CatalogueItemTo(long id, UUID guid, String caption, String description) {

    public static CatalogueItemTo of(final CatalogueItemEntity entity) {
        requireNonNull(entity, "Entity cannot be null");
        return new CatalogueItemTo(entity.id(), entity.guid(), entity.caption(), entity.description());
    }

    public <T> T map(final Function<CatalogueItemTo, T> mapper) {
        requireNonNull(mapper, "Mapper cannot be nul");
        return mapper.apply(this);
    }
}
