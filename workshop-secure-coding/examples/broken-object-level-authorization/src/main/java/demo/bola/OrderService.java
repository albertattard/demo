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

    public CustomerOrder findOrder(final Long orderId) {
        return repository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> findOrdersFor(final String username) {
        return repository.findByOwnerUsernameOrderById(username);
    }

    @Transactional
    public void updateDescription(final Long orderId, final String description) {
        findOrder(orderId)
                .changeDescription(description);
    }
}
