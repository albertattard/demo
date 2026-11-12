package demo.cart;

import demo.external.catalogue.CatalogueItemEntity;
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "itemId")
    private CatalogueItemEntity item;

    protected CartItemEntityPrimaryKey() {}

    public CartItemEntityPrimaryKey(final CartEntity cart, final CatalogueItemEntity item) {
        this.cart = cart;
        this.item = item;
    }

    public Long cartId() {
        return mapIfNotNull(cart, CartEntity::id);
    }

    public Long itemId() {
        return mapIfNotNull(item, CatalogueItemEntity::id);
    }

    public String itemCaption() {
        return mapIfNotNull(item, CatalogueItemEntity::caption);
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
        return "CartItemEntityPrimaryKey[cartId=" + cartId() + ", itemId=" + itemId() + ']';
    }
}
