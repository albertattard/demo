package demo.catalogue;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/catalogue/item")
public final class CatalogueItemController {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final CatalogueItemService service;
    private Bandwidth limit;
    private final Map<String, Bucket> ipToBucket = new HashMap<>();

    public CatalogueItemController(final CatalogueItemService service) {
        this.service = requireNonNull(service, "Service cannot be null");

        // Configure the rate limit
        limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
    }

    private Bucket getBucket(String remoteAddress) {
        log.info("retrieving token bucket for user at {}", remoteAddress);

        Bucket result = ipToBucket.get(remoteAddress);
        if (result == null) {
            result = Bucket.builder()
                    .addLimit(limit)
                    .build();
            ipToBucket.put(remoteAddress, result);
        }
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogueItemTo> get(@PathVariable(value = "id") final String id, HttpServletRequest request) {
        // Here's where the basic rate limit happens
        Bucket bucket = getBucket(request.getRemoteAddr());
        if (! bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        Long longId = null;
        try {
            longId = Long.valueOf(id);
        } catch (NumberFormatException e) {
            // ignored
        }
        if (longId != null) {
            return service.findById(longId)
                    .map(CatalogueItemTo::of)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }
        return service.findByGuid(UUID.fromString(id))
                .map(CatalogueItemTo::of)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all/{id}")
    public ResponseEntity<List<CatalogueItemTo>> getAll(@PathVariable(value = "id") final Set<Long> ids,
            HttpServletRequest request) {
        Bucket bucket = getBucket(request.getRemoteAddr());
        if (! bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        return ResponseEntity.ok(
                service.findAllById(ids).stream()
                        .map(CatalogueItemTo::of)
                        .collect(Collectors.toList()));
    }

    @PostMapping()
    public ResponseEntity<CatalogueItemTo> add(@RequestBody final NewCatalogueItemTo item, HttpServletRequest request) {
        Bucket bucket = getBucket(request.getRemoteAddr());
        if (! bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        return item.toEntity()
                .map(service::add)
                .map(CatalogueItemTo::of)
                .map(e -> ResponseEntity.created(location(e)).body(e));
    }

    private static URI location(final CatalogueItemTo item) {
        requireNonNull(item, "Catalogue item cannot be null");
        return URI.create("/catalogue/item/%d".formatted(item.id()));
    }
}
