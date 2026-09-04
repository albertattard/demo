package demo.catalogue;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OfferService {

    private final OfferRepository repository;

    public OfferService(final OfferRepository repository) {
        this.repository = repository;
    }

    public List<OfferSummary> findBySlugIn(final List<String> orderedSlugs) {
        return repository.findBySlugIn(orderedSlugs);
    }

    public List<OfferSummary> search(final String q, final BigDecimal minPrice, final BigDecimal maxPrice) {
        // Treat blank as null so the JPQL null-guards kick in
        final String qNormalised = (q == null || q.isBlank() ? null : q.trim());
        return repository.search(qNormalised, minPrice, maxPrice);
    }

    public List<OfferEntity> findAll() {
        return repository.findAll();
    }

    public Optional<OfferEntity> findBySlug(final String slug) {
        return repository.findBySlug(slug);
    }
}
