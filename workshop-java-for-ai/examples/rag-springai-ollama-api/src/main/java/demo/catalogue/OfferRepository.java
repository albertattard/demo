package demo.catalogue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<OfferEntity, Long> {

    List<OfferSummary> findBySlugIn(Collection<String> slugs);

    @Query("""
            SELECT o FROM OfferEntity o
            WHERE (:q IS NULL OR
                   LOWER(o.title)                   LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(o.subtitle)                LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(CONCAT('', o.description)) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:minPrice IS NULL OR o.price >= :minPrice)
              AND (:maxPrice IS NULL OR o.price <= :maxPrice)
            ORDER BY o.id ASC""")
    List<OfferSummary> search(
            @Param("q") String q,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    Optional<OfferEntity> findBySlug(String slug);
}
