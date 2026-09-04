package demo.catalogue;

import jakarta.persistence.*;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "offer")
public class OfferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 140)
    private String slug;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, length = 300)
    private String subtitle;

    @Column(nullable = false)
    @Lob
    private String description;

    @Column(nullable = false)
    @NumberFormat(style = NumberFormat.Style.CURRENCY)
    private BigDecimal price;

    @Column(nullable = false, length = 300)
    private String priceDescription;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "offer_id", nullable = false)
    @OrderBy("id ASC")
    private List<TripHighlightEntity> highlights = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "offer_id", nullable = false)
    @OrderBy("id ASC")
    private List<ItineraryEntity> itinerary = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "offer_id", nullable = false)
    @OrderBy("id ASC")
    private List<PackageDetailEntity> packageDetails = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "offer_id", nullable = false)
    @OrderBy("id ASC")
    private List<BestTravelSeasonEntity> bestTravelSeasons = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getPriceDescription() {
        return priceDescription;
    }

    public List<TripHighlightEntity> getHighlights() {
        return highlights;
    }

    public List<ItineraryEntity> getItinerary() {
        return itinerary;
    }

    public List<PackageDetailEntity> getPackageDetails() {
        return packageDetails;
    }

    public List<BestTravelSeasonEntity> getBestTravelSeasons() {
        return bestTravelSeasons;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    void onCreate() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof final OfferEntity other
               && Objects.equals(id, other.id)
               && Objects.equals(slug, other.slug)
               && Objects.equals(title, other.title)
               && Objects.equals(subtitle, other.subtitle)
               && Objects.equals(description, other.description)
               && Objects.equals(price, other.price)
               && Objects.equals(priceDescription, other.priceDescription)
               && Objects.equals(highlights, other.highlights)
               && Objects.equals(itinerary, other.itinerary)
               && Objects.equals(packageDetails, other.packageDetails)
               && Objects.equals(bestTravelSeasons, other.bestTravelSeasons)
               && Objects.equals(createdAt, other.createdAt)
               && Objects.equals(updatedAt, other.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, slug, title, subtitle, description, price, priceDescription, highlights, itinerary, packageDetails, bestTravelSeasons, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "BrochureEntity[" +
               "id=" + id +
               ", slug='" + slug + '\'' +
               ", title='" + title + '\'' +
               ", subtitle='" + subtitle + '\'' +
               ", description='" + description + '\'' +
               ", price=" + price +
               ", priceDescription='" + priceDescription + '\'' +
               ", highlights='" + highlights + '\'' +
               ", itinerary='" + itinerary + '\'' +
               ", packageDetails='" + packageDetails + '\'' +
               ", bestTravelSeasons='" + bestTravelSeasons + '\'' +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               ']';
    }
}
