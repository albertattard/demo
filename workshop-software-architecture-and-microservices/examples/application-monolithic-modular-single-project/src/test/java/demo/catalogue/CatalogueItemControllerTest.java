package demo.catalogue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class CatalogueItemControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

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
            return restTemplate.getForEntity("/catalogue/item/" + id, CatalogueItemTo.class);
        }
    }

    @Nested
    class AddCatalogueItemTest {

        @Test
        void returnTheNewIdOfTheAddedCatalogueItem() {
            final NewCatalogueItemTo item = new NewCatalogueItemTo("Green Plant", "Put a little life in your living room!!");

            final ResponseEntity<CatalogueItemTo> response = restTemplate.postForEntity("/catalogue/item", item, CatalogueItemTo.class);
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
}
