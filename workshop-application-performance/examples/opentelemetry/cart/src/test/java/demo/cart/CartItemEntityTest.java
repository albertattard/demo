package demo.cart;

import demo.catalogue.CatalogueItemEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class CartItemEntityTest {

    @Test
    void cartItemUsesCaptionFromCatalogueItemWhenSet() {
        /* Given */
        final CatalogueItemEntity catalogueItem = new CatalogueItemEntity(1L, "Test Item");
        final CartEntity cart = new CartEntity(1L, new ArrayList<>());
        final CartItemEntity cartItem = new CartItemEntity(new CartItemEntityPrimaryKey(cart, catalogueItem.id()), 1);
        cart.items().add(cartItem);

        /* When */
        cartItem.catalogueItemEntity(catalogueItem);

        /* Then */
        assertThat(cartItem.itemCaption())
                .isEqualTo(catalogueItem.caption());
    }
}
