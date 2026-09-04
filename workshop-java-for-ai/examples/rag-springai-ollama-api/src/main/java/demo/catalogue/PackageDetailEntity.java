package demo.catalogue;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "package_detail")
public class PackageDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", nullable = false, length = 300)
    private String description;

    public Long getId() {return id;}

    public String getDescription() {return description;}

    @Override
    public boolean equals(final Object object) {
        return object instanceof final PackageDetailEntity other
               && Objects.equals(id, other.id)
               && Objects.equals(description, other.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description);
    }

    @Override
    public String toString() {
        return "PackageDetailEntity[" +
               "id=" + id +
               ", description='" + description + '\'' +
               ']';
    }
}
