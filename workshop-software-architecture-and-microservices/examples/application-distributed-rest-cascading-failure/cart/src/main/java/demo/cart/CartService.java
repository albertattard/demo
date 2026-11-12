package demo.cart;

import demo.external.catalogue.CatalogueItemEntity;
import demo.external.catalogue.CatalogueItemGateway;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CatalogueItemGateway itemGateway;

    public CartService(final CartRepository cartRepository,
                       final CatalogueItemGateway itemGateway) {
        this.cartRepository = requireNonNull(cartRepository, "Cart repository cannot be null");
        this.itemGateway = requireNonNull(itemGateway, "Catalogue item gateway cannot be null");
    }

    public Optional<CartEntity> findById(final long id) {
        return cartRepository.findById(id)
                .map(this::fetchCartItemsCaption);
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
            final Optional<CatalogueItemEntity> optionalItem = itemGateway.findById(itemId);

            /* Catalogue Item not found */
            if (optionalItem.isEmpty()) {
                return Optional.empty();
            }

            cart.addItem(optionalItem.get(), 1);
        } else {
            itemInCart.adjustQuantityBy(1);
        }

        final CartEntity savedCart = cartRepository.save(cart);
        return Optional.of(fetchCartItemsCaption(savedCart));
    }

    private CartEntity fetchCartItemsCaption(final CartEntity cart) {
        final List<CartItemEntity> items = cart.items();
        final Set<Long> cartItemIds = items.stream()
                .map(CartItemEntity::itemId)
                .collect(Collectors.toSet());

        final Map<Long, CatalogueItemEntity> catalogueItems = itemGateway.findAllById(cartItemIds)
                .stream()
                .collect(Collectors.toMap(CatalogueItemEntity::id, Function.identity()));

        for (CartItemEntity item : cart.items()) {
            final Long id = item.itemId();
            final CatalogueItemEntity entity = catalogueItems.get(id);
            if (entity != null) {
                item.catalogueItemEntity(entity);
            }
        }

        return cart;
    }
}
