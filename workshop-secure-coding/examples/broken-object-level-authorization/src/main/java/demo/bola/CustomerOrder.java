package demo.bola;

import jakarta.persistence.*;

@Entity
@Table(name = "customer_order")
public class CustomerOrder {

    @Id
    private Long id;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    protected CustomerOrder() {}

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public UserAccount getOwner() {
        return owner;
    }

    public void changeDescription(final String description) {
        this.description = description;
    }
}
