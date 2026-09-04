package demo.bola;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    @EntityGraph(attributePaths = "owner")
    Optional<CustomerOrder> findByIdAndOwnerUsername(Long id, String username);

    List<CustomerOrder> findByOwnerUsernameOrderById(String username);
}
