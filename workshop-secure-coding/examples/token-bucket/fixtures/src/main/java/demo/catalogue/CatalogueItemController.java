package demo.catalogue;

import demo.ratelimit.ClientRateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/catalogue/item")
public final class CatalogueItemController {

    private static final int READ_COST = 1;
    private static final int BULK_READ_COST = 5;

    private final CatalogueItemService service;
    private final ClientRateLimiter rateLimiter;

    public CatalogueItemController(
            final CatalogueItemService service,
            final ClientRateLimiter rateLimiter) {
        this.service = requireNonNull(service, "Service cannot be null");
        this.rateLimiter = requireNonNull(rateLimiter, "Rate limiter cannot be null");
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogueItemTo> get(
            final @PathVariable long id,
            final Principal principal) {
        if (!admit(principal, READ_COST)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        return service.findById(id)
                .map(CatalogueItemTo::of)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all/{id}")
    public ResponseEntity<List<CatalogueItemTo>> getAll(
            final @PathVariable("id") Set<Long> ids,
            final Principal principal) {
        if (!admit(principal, BULK_READ_COST)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        return ResponseEntity.ok(
                service.findAllById(ids).stream()
                        .map(CatalogueItemTo::of)
                        .collect(Collectors.toList()));
    }

    private boolean admit(final Principal principal, final int cost) {
        return rateLimiter.tryConsume(principal.getName(), cost);
    }
}
