package demo.cart;

import demo.catalogue.CatalogueItemEntity;
import demo.catalogue.CatalogueItemGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class CartServiceTest {

    private final CartRepository repository = mock(CartRepository.class);
    private final CatalogueItemGateway itemGateway = mock(CatalogueItemGateway.class);

    private final CartService service = new CartService(repository, itemGateway);

    @BeforeEach
    void setUp() {
        reset(repository, itemGateway);
    }

    @Nested
    class AddItemToCart {

        @Test
        void addItemToNewCart() {
            /* Given */
            final CartEntity cart = new CartEntity(1L, new ArrayList<>());
            final CatalogueItemEntity catalogueItem = new CatalogueItemEntity(1L, "Test Item");
            when(repository.findById(1L)).thenReturn(Optional.of(cart));
            when(itemGateway.findById(1L)).thenReturn(Optional.of(catalogueItem));
            when(repository.save(cart)).thenReturn(cart);

            /* When */
            final Optional<CartEntity> saved = service.addItemToCart(1, 1);

            /* Then */
            final CartEntity expectedCart = new CartEntity(1L, new ArrayList<>());
            final CartItemEntity expectedCartItem = new CartItemEntity(new CartItemEntityPrimaryKey(expectedCart, 1L), 1);
            expectedCartItem.catalogueItemEntity(catalogueItem);
            expectedCart.items().add(expectedCartItem);
            assertThat(saved)
                    .isEqualTo(Optional.of(expectedCart));
        }
    }

    @Nested
    class FindWithId {

        @Test
        void returnCartWithItems() {
            /* Given */
            final CatalogueItemEntity catalogueItem = new CatalogueItemEntity(1L, "Test Item");
            final CartEntity cart = new CartEntity(1L, new ArrayList<>());
            final CartItemEntity cartItem = new CartItemEntity(new CartItemEntityPrimaryKey(cart, catalogueItem.id()), 1);
            cart.items().add(cartItem);
            when(repository.findById(1L)).thenReturn(Optional.of(cart));
            when(itemGateway.findById(1L)).thenReturn(Optional.of(catalogueItem));

            /* When */
            final Optional<CartEntity> result = service.findById(1L);

            /* Then */
            final CartEntity expectedCart = new CartEntity(1L, new ArrayList<>());
            final CartItemEntity expectedCartItem = new CartItemEntity(new CartItemEntityPrimaryKey(expectedCart, catalogueItem.id()), 1);
            expectedCartItem.catalogueItemEntity(catalogueItem);
            expectedCart.items().add(expectedCartItem);
            assertThat(result)
                    .isEqualTo(Optional.of(expectedCart));
        }
    }
}