package demo.catalogue;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "itinerary")
public class ItineraryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "itinerary_id", nullable = false)
    @OrderBy("id ASC")
    private List<ItineraryEntryEntity> entries = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<ItineraryEntryEntity> getEntries() {
        return entries;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof final ItineraryEntity other
               && Objects.equals(id, other.id)
               && Objects.equals(title, other.title)
               && Objects.equals(entries, other.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, entries);
    }

    @Override
    public String toString() {
        return "ItineraryEntity[" +
               "id=" + id +
               ", title='" + title + '\'' +
               ", entries='" + entries + '\'' +
               ']';
    }
}
