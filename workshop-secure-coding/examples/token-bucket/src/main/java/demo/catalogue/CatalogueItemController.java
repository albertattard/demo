package demo.catalogue;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/catalogue/item")
public final class CatalogueItemController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogueItemController.class);

    private final CatalogueItemService service;
    private final Bandwidth limit;
    private final Map<String, Bucket> ipToBucket = new HashMap<>();

    public CatalogueItemController(final CatalogueItemService service) {
        this.service = requireNonNull(service, "Service cannot be null");

        // Configure the rate limit
        limit = Bandwidth.builder()
                .capacity(20)
                .refillGreedy(20, Duration.ofMinutes(1))
                .build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogueItemTo> get(
            @PathVariable(value = "id") final long id,
            final HttpServletRequest request) {
        // Here's where the basic rate limit happens
        final Bucket bucket = getBucket(request.getRemoteAddr());
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        return service.findById(id)
                .map(CatalogueItemTo::of)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all/{id}")
    public ResponseEntity<List<CatalogueItemTo>> getAll(
            @PathVariable(value = "id") final Set<Long> ids,
            final HttpServletRequest request) {
        final Bucket bucket = getBucket(request.getRemoteAddr());
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        return ResponseEntity.ok(
                service.findAllById(ids).stream()
                        .map(CatalogueItemTo::of)
                        .collect(Collectors.toList()));
    }

    private Bucket getBucket(final String remoteAddress) {
        LOGGER.info("retrieving token bucket for user at {}", remoteAddress);

        Bucket result = ipToBucket.get(remoteAddress);
        if (result == null) {
            result = Bucket.builder()
                    .addLimit(limit)
                    .build();
            ipToBucket.put(remoteAddress, result);
        }
        return result;
    }
}
