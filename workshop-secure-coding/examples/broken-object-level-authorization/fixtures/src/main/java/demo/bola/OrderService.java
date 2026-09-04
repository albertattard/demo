package demo.bola;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final CustomerOrderRepository repository;

    OrderService(final CustomerOrderRepository repository) {
        this.repository = repository;
    }

    public CustomerOrder findOwnedOrder(final Long orderId, final String username) {
        return repository.findByIdAndOwnerUsername(orderId, username)
                .orElseThrow(OrderNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> findOrdersFor(final String username) {
        return repository.findByOwnerUsernameOrderById(username);
    }

    @Transactional
    public void updateOwnedDescription(final Long orderId, final String username, final String description) {
        findOwnedOrder(orderId, username)
                .changeDescription(description);
    }
}
