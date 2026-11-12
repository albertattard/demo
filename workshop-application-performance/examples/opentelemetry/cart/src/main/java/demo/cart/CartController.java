package demo.cart;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService service;

    public CartController(final CartService service) {
        this.service = requireNonNull(service, "Service cannot be null");
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartTo> get(@PathVariable(value = "id") final long id) {
        return service.findById(id)
                .map(CartTo::of)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{cartId}/item/{itemId}")
    public ResponseEntity<CartTo> addItemToCart(@PathVariable(value = "cartId") final long cartId,
                                                @PathVariable(value = "itemId") final long itemId) {
        return service.addItemToCart(cartId, itemId)
                .map(CartTo::of)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
