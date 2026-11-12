package demo.cart;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class CartControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private static final ClientAndServer catalogueServer = startClientAndServer(0);

    @DynamicPropertySource
    static void catalogueProperties(final DynamicPropertyRegistry registry) {
        registry.add("catalogue.root-uri", () -> "http://localhost:" + catalogueServer.getLocalPort());
    }

    @BeforeAll
    static void startServer() {
        catalogueServer.when(request()
                        .withMethod("GET")
                        .withPath("/catalogue/item/all/1,5"))
                .respond(response()
                        .withStatusCode(200)
                        .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"))
                        .withBody("""
                                [
                                  {
                                    "id":1,
                                    "caption":"Leather Sofa",
                                    "description":"A very nice and comfortable sofa"
                                  },
                                  {
                                    "id":5,
                                    "caption":"LED TV",
                                    "description":"A very large TV set, ideal for those who love to binge-watch TV shows"
                                  }
                                ]"""));

        catalogueServer.when(request()
                        .withMethod("GET")
                        .withPath("/catalogue/item/all/2,3,4"))
                .respond(response()
                        .withStatusCode(200)
                        .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"))
                        .withBody("""
                                [
                                  {
                                    "id":2,
                                    "caption":"Wooden Table",
                                    "description":"A large table ideal for 6 to 8 people"
                                  },
                                  {
                                    "id":3,
                                    "caption":"Plastic Chair",
                                    "description":"A robust plastic chair ideal for children and adults alike"
                                  },
                                  {
                                    "id":4,
                                    "caption":"Mug",
                                    "description":"The ideal way to start the day"
                                  }
                                ]"""));

        catalogueServer.when(request()
                        .withMethod("GET")
                        .withPath("/catalogue/item/all/4"))
                .respond(response()
                        .withStatusCode(200)
                        .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"))
                        .withBody("""
                                [
                                  {
                                    "id":4,
                                    "caption":"Mug",
                                    "description":"The ideal way to start the day"
                                  }
                                ]"""));

        catalogueServer.when(request()
                        .withMethod("GET")
                        .withPath("/catalogue/item/4"))
                .respond(response()
                        .withStatusCode(200)
                        .withHeaders(new Header("Content-Type", "application/json; charset=utf-8"))
                        .withBody("""
                                {
                                  "id":4,
                                  "caption":"Mug",
                                  "description":"The ideal way to start the day"
                                }"""));

        catalogueServer.when(request()
                        .withMethod("GET")
                        .withPath("/catalogue/item/10"))
                .respond(response()
                        .withStatusCode(404));
    }

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
            return restTemplate.getForEntity(baseUrl() + "/cart/" + id, CartTo.class);
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
            return restTemplate.postForEntity(baseUrl() + "/cart/" + cartId + "/item/" + itemId, Map.of(), CartTo.class);
        }
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @AfterAll
    static void stopServer() {
        catalogueServer.stop();
    }
}
