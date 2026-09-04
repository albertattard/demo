package demo.catalogue;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/catalogue/item")
public final class CatalogueItemController {

    private final CatalogueItemService service;

    public CatalogueItemController(final CatalogueItemService service) {
        this.service = requireNonNull(service, "Service cannot be null");
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogueItemTo> get(@PathVariable(value = "id") final String id) {
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
    public ResponseEntity<List<CatalogueItemTo>> getAll(@PathVariable(value = "id") final Set<Long> ids) {
        return ResponseEntity.ok(
                service.findAllById(ids).stream()
                        .map(CatalogueItemTo::of)
                        .collect(Collectors.toList()));
    }

    @PostMapping()
    public ResponseEntity<CatalogueItemTo> add(@RequestBody final NewCatalogueItemTo item) {
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
