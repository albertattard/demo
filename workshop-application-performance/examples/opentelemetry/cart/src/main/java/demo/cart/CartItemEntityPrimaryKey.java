package demo.cart;

import demo.catalogue.CatalogueItemEntity;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import static demo.common.Mapper.mapIfNotNull;

@Embeddable
public class CartItemEntityPrimaryKey implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cartId")
    private CartEntity cart;
    private Long itemId;

    /* This is not persisted, and it attached to the entity at a later stage */
    private transient CatalogueItemEntity item;

    protected CartItemEntityPrimaryKey() {}

    public CartItemEntityPrimaryKey(final CartEntity cart, final Long itemId) {
        this.cart = cart;
        this.itemId = itemId;
    }

    public Long cartId() {
        return mapIfNotNull(cart, CartEntity::id);
    }

    public Long itemId() {
        return itemId;
    }

    public String itemCaption() {
        return mapIfNotNull(item, CatalogueItemEntity::caption);
    }

    public void catalogueItemEntity(final CatalogueItemEntity item) {
        if (item != null && item.id() != itemId) {
            throw new IllegalArgumentException("Catalogue entity does not match item id");
        }

        this.item = item;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof final CartItemEntityPrimaryKey other
               && getClass() == other.getClass()
               && Objects.equals(cartId(), other.cartId())
               && Objects.equals(itemId(), other.itemId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartId(), itemId());
    }

    @Override
    public String toString() {
        return "CartItemEntityPrimaryKey[cartId=%d, itemId=%d]".formatted(cartId(), itemId());
    }
}
