package demo.catalogue;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Set;
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
    public ResponseEntity<CatalogueItemTo> get(@PathVariable(value = "id") final long id) {
        return service.findById(id)
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
        return URI.create("/catalogue/item/" + item.id());
    }
}
