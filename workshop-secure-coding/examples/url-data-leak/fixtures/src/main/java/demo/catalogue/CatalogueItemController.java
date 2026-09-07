package demo.catalogue;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/catalogue/item")
public final class CatalogueItemController {

    private final CatalogueItemService service;

    public CatalogueItemController(final CatalogueItemService service) {
        this.service = requireNonNull(service, "Service cannot be null");
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogueItemTo> get(@PathVariable(value = "id") final UUID id) {
        return service.findByGuid(id)
                .map(CatalogueItemTo::of)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
