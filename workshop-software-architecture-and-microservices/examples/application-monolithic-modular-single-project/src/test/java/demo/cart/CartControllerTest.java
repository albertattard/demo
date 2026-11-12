package demo.cart;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class CartControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Nested
    class GetCartTest {

        @Test
        void returnTheCartWithItemsWhenGivenAnExistingId() {
            final ResponseEntity<CartTo> response = getCartWithId(1);
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody())
                    .isEqualTo(new CartTo(1L, List.of(
                            new CartItemTo(1L, "Leather Sofa", 1),
                            new CartItemTo(5L, "LED TV", 1)
                    )));
        }

        @Test
        void returnNotFoundWhenGivenAnIdThatDoesNotExists() {
            final ResponseEntity<CartTo> response = getCartWithId(10);
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }

        private ResponseEntity<CartTo> getCartWithId(final long id) {
            return restTemplate.getForEntity("/cart/" + id, CartTo.class);
        }
    }

    @Nested
    class AddItemToCartTest {

        @Test
        void returnTheCartWithTheNewItemWhenTheItemIsNotAlreadyInTheCart() {
            final ResponseEntity<CartTo> response = addItemToCart(2, 4);
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody())
                    .isEqualTo(new CartTo(2L, List.of(
                            new CartItemTo(2L, "Wooden Table", 1),
                            new CartItemTo(3L, "Plastic Chair", 6),
                            new CartItemTo(4L, "Mug", 1)
                    )));
        }

        @Test
        void returnTheCartWithTheNewQuantityWhenTheItemIsAlreadyInTheCart() {
            final ResponseEntity<CartTo> response = addItemToCart(3, 4);
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody())
                    .isEqualTo(new CartTo(3L, List.of(
                            new CartItemTo(4L, "Mug", 5)
                    )));
        }

        @Test
        void returnNotFoundWhenGivenACartIdThatDoesNotExists() {
            final ResponseEntity<CartTo> response = addItemToCart(10, 1);
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void returnNotFoundWhenGivenAnItemIdThatDoesNotExists() {
            final ResponseEntity<CartTo> response = addItemToCart(1, 10);
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }

        private ResponseEntity<CartTo> addItemToCart(final long cartId, final long itemId) {
            return restTemplate.postForEntity("/cart/" + cartId + "/item/" + itemId, Map.of(), CartTo.class);
        }
    }
}
