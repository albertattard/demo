package demo.catalogue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class CatalogueItemControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Nested
    class GetCatalogueItemTest {

        @Test
        void returnTheCatalogueItemWhenGivenAnExistingId() {
            final ResponseEntity<CatalogueItemTo> response = getCatalogueItemWithId(1);
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody())
                    .isEqualTo(new CatalogueItemTo(1L, "Leather Sofa", "A very nice and comfortable sofa"));
        }

        @Test
        void returnNotFoundWhenGivenAnIdThatDoesNotExists() {
            final ResponseEntity<CatalogueItemTo> response = getCatalogueItemWithId(10);
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }

        private ResponseEntity<CatalogueItemTo> getCatalogueItemWithId(final long id) {
            return restTemplate.getForEntity(baseUrl() + "/catalogue/item/" + id, CatalogueItemTo.class);
        }
    }

    @Nested
    class GetAllCatalogueItemsTest {

        @Test
        void returnTheCatalogueItemsWithTheGivenExistingId() {
            final ResponseEntity<List<CatalogueItemTo>> response = getCatalogueItemsWithId(1, 2, 3, 4);
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()) /* TODO: May need to sort the result */
                    .isEqualTo(List.of(
                            new CatalogueItemTo(1L, "Leather Sofa", "A very nice and comfortable sofa"),
                            new CatalogueItemTo(2L, "Wooden Table", "A large table ideal for 6 to 8 people"),
                            new CatalogueItemTo(3L, "Plastic Chair", "A robust plastic chair ideal for children and adults alike"),
                            new CatalogueItemTo(4L, "Mug", "The ideal way to start the day")
                    ));
        }

        @Test
        void returnTheSameItemOnceEvenWhenItsIdIsProvidedMultipleTimes() {
            final ResponseEntity<List<CatalogueItemTo>> response = getCatalogueItemsWithId(1, 1, 1, 1);
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody())
                    .isEqualTo(List.of(
                            new CatalogueItemTo(1L, "Leather Sofa", "A very nice and comfortable sofa")
                    ));
        }

        @Test
        void returnOnlyTheFoundWhenGivenAnIdThatDoesNotExists() {
            final ResponseEntity<List<CatalogueItemTo>> response = getCatalogueItemsWithId(1, 10);
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody())
                    .isEqualTo(List.of(new CatalogueItemTo(1L, "Leather Sofa", "A very nice and comfortable sofa")));
        }

        @Test
        void returnEmptyListWhenGivenAnIdThatDoesNotExists() {
            final ResponseEntity<List<CatalogueItemTo>> response = getCatalogueItemsWithId(10);
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody())
                    .isEqualTo(List.of());
        }

        private ResponseEntity<List<CatalogueItemTo>> getCatalogueItemsWithId(final long... ids) {
            final String stringIds = LongStream.of(ids).mapToObj(String::valueOf).collect(Collectors.joining(","));
            final ParameterizedTypeReference<List<CatalogueItemTo>> type = new ParameterizedTypeReference<>() {};
            return restTemplate.exchange(baseUrl() + "/catalogue/item/all/" + stringIds, HttpMethod.GET, null, type);
        }
    }

    @Nested
    class AddCatalogueItemTest {

        @Test
        void returnTheNewIdOfTheAddedCatalogueItem() {
            final NewCatalogueItemTo item = new NewCatalogueItemTo("Green Plant", "Put a little life in your living room!!");

            final ResponseEntity<CatalogueItemTo> response = restTemplate.postForEntity(baseUrl() + "/catalogue/item", item, CatalogueItemTo.class);
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.CREATED);

            final long id = assertThatHeaderLocationHasCatalogueItemId(response);

            assertThat(response.getBody())
                    .isEqualTo(new CatalogueItemTo(id, item.caption(), item.description()));
        }

        private static long assertThatHeaderLocationHasCatalogueItemId(final ResponseEntity<CatalogueItemTo> response) {
            assertThat(response.getHeaders())
                    .isNotNull();
            assertThat(response.getHeaders().getLocation())
                    .isNotNull();

            final String location = response.getHeaders().getLocation().getPath();
            assertThat(location)
                    .matches(Pattern.compile("/catalogue/item/\\d+"));

            return Long.parseLong(location.substring(16));
        }
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
