package demo.catalogue;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "best_travel_season")
public class BestTravelSeasonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "season", nullable = false, length = 300)
    private String season;

    @Column(name = "description", nullable = false, length = 300)
    private String description;

    public Long getId() {
        return id;
    }

    public String getSeason() {
        return season;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof final BestTravelSeasonEntity other
               && Objects.equals(id, other.id)
               && Objects.equals(season, other.season)
               && Objects.equals(description, other.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, season, description);
    }

    @Override
    public String toString() {
        return "BestTravelSeasonEntity[" +
               "id=" + id +
               ", season='" + season + '\'' +
               ", description='" + description + '\'' +
               ']';
    }
}
