package demo.cart;

import static java.util.Objects.requireNonNull;

public record CartItemTo(Long id, String caption, int quantity) {

    public static CartItemTo of(final CartItemEntity entity) {
        requireNonNull(entity, "Entity cannot be null");

        final Long id = entity.itemId();
        final String caption = entity.itemCaption();
        final int quantity = entity.quantity();

        return new CartItemTo(id, caption, quantity);
    }
}
