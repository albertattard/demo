package demo.catalogue;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@Entity(name = "catalogue_item")
public class CatalogueItemEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String caption;
    private String description;

    protected CatalogueItemEntity() {}

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
        return object instanceof final CatalogueItemEntity other
               && getClass() == other.getClass()
               && Objects.equals(id, other.id)
               && Objects.equals(caption, other.caption)
               && Objects.equals(description, other.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, caption, description);
    }

    @Override
    public String toString() {
        return "CatalogueItemEntity[id=%d, caption=%s, description=%s]".formatted(id, caption, description);
    }
}
