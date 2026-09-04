package demo.catalogue;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "trip_highlight")
public class TripHighlightEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "highlight", nullable = false, length = 300)
    private String highlight;

    public Long getId() {return id;}

    public String getHighlight() {return highlight;}

    @Override
    public boolean equals(final Object object) {
        return object instanceof final TripHighlightEntity other
               && Objects.equals(id, other.id)
               && Objects.equals(highlight, other.highlight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, highlight);
    }

    @Override
    public String toString() {
        return "TripHighlightEntity[" +
               "id=" + id +
               ", highlight='" + highlight + '\'' +
               ']';
    }
}
