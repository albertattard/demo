package demo.cart;

import demo.catalogue.CatalogueItemEntity;
import demo.catalogue.CatalogueItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CatalogueItemRepository itemRepository;

    public CartService(final CartRepository cartRepository,
                       final CatalogueItemRepository itemRepository) {
        this.cartRepository = requireNonNull(cartRepository, "Cart repository cannot be null");
        this.itemRepository = requireNonNull(itemRepository, "Catalogue item repository cannot be null");
    }

    public Optional<CartEntity> findById(final long id) {
        return cartRepository.findById(id);
    }

    @Transactional
    public Optional<CartEntity> addItemToCart(final long cartId, final long itemId) {
        final Optional<CartEntity> optionalCart = cartRepository.findById(cartId);

        /* Cart not found */
        if (optionalCart.isEmpty()) {
            return Optional.empty();
        }

        final CartEntity cart = optionalCart.get();

        final CartItemEntity itemInCart = cart.findCartItemWithId(itemId);
        if (itemInCart == null) {
            final Optional<CatalogueItemEntity> optionalItem = itemRepository.findById(itemId);

            /* Catalogue Item not found */
            if (optionalItem.isEmpty()) {
                return Optional.empty();
            }

            cart.addItem(optionalItem.get(), 1);
        } else {
            itemInCart.adjustQuantityBy(1);
        }

        final CartEntity savedCart = cartRepository.save(cart);
        return Optional.of(savedCart);
    }
}
