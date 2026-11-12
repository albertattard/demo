package demo.rest;

import demo.service.CatalogueItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
