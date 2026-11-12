package demo.cart;

import org.springframework.data.repository.ListCrudRepository;

public interface CartRepository extends ListCrudRepository<CartEntity, Long> {}
