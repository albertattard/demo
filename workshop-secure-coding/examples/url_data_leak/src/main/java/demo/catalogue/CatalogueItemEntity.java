package demo.catalogue;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity(name = "catalogue_item")
public final class CatalogueItemEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID guid;
    private String caption;
    private String description;

    protected CatalogueItemEntity() {
    }

    public CatalogueItemEntity(final String caption, final String description) {
        this.caption = caption;
        this.description = description;
    }

    public CatalogueItemEntity(final long id, final String caption, final String description) {
        this.id = id;
        this.caption = caption;
        this.description = description;
    }

    public Long id() {
        return id;
    }

    public UUID guid() {
        return guid;
    }

    public String caption() {
        return caption;
    }

    public String description() {
        return description;
    }

    public <T> T map(final Function<CatalogueItemEntity, T> mapper) {
        requireNonNull(mapper, "Mapper cannot be nul");
        return mapper.apply(this);
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof final CatalogueItemEntity other && getClass() == other.getClass()
                && Objects.equals(id, other.id) && Objects.equals(guid, other.guid)
                && Objects.equals(caption, other.caption) && Objects.equals(description, other.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, guid, caption, description);
    }

    @Override
    public String toString() {
        return "CatalogueItemEntity[id=%d, guid=%s, caption=%s, description=%s]".formatted(id, guid, caption,
                description);
    }
}
