package demo.dao;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import static demo.common.Mapper.mapIfNotNull;

@Entity(name = "cart_item")
public class CartItemEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private CartItemEntityPrimaryKey id;
    private int quantity;

    protected CartItemEntity() {}

    public CartItemEntity(final CartItemEntityPrimaryKey id, final int quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    public CartItemEntityPrimaryKey id() {
        return id;
    }

    public Long itemId() {
        return mapIfNotNull(id, CartItemEntityPrimaryKey::itemId);
    }

    public String itemCaption() {
        return mapIfNotNull(id, CartItemEntityPrimaryKey::itemCaption);
    }

    public int quantity() {
        return quantity;
    }

    public void adjustQuantityBy(final int offset) {
        this.quantity += offset;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof final CartItemEntity other
                && getClass() == other.getClass()
                && Objects.equals(id, other.id)
                && quantity == other.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, quantity);
    }

    @Override
    public String toString() {
        return "CartItemEntity[id=" + id + ", quantity=" + quantity + ']';
    }
}
