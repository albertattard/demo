package demo.cart;

import demo.external.catalogue.CatalogueItemEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

@Entity(name = "cart")
public class CartEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "id.cart", cascade = CascadeType.ALL)
    private List<CartItemEntity> items;

    protected CartEntity() {}

    public Long id() {
        return id;
    }

    public List<CartItemEntity> items() {
        return items;
    }

    public void addItem(final CatalogueItemEntity item, final int quantity) {
        requireNonNull(item, "Catalogue item cannot be null");

        final CartItemEntityPrimaryKey cartItemId = new CartItemEntityPrimaryKey(this, item);
        final CartItemEntity cartItem = new CartItemEntity(cartItemId, quantity);
        items.add(cartItem);
    }

    public CartItemEntity findCartItemWithId(final Long id) {
        return items.stream()
                .filter(i -> id.equals(i.itemId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof final CartEntity other
                && getClass() == other.getClass()
                && Objects.equals(id, other.id)
                && Objects.equals(items, other.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, items);
    }

    @Override
    public String toString() {
        return "CartEntity[id=" + id + ", items=" + items + ']';
    }
}
