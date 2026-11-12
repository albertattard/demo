package demo.catalogue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
    class AddCatalogueItemTest {

        @Autowired
        private RabbitTemplate rabbitTemplate;

        @Test
        void returnTheNewIdOfTheAddedCatalogueItem() {
            final NewCatalogueItemTo item = new NewCatalogueItemTo("Green Plant", "Put a little life in your living room!!");

            final ResponseEntity<CatalogueItemTo> response = restTemplate.postForEntity(baseUrl() + "/catalogue/item", item, CatalogueItemTo.class);
            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.CREATED);

            final long id = assertThatHeaderLocationHasCatalogueItemId(response);

            assertThat(response.getBody())
                    .isEqualTo(new CatalogueItemTo(id, item.caption(), item.description()));

            final Message received = rabbitTemplate.receive("demo-queue", TimeUnit.SECONDS.toMillis(5));
            assertThat(received)
                    .describedAs("The message should be sent to the queue")
                    .isNotNull();

            assertThat(received.getMessageProperties().getHeaders().get("__TypeId__"))
                    .describedAs("The type is used by consumers to map this object to their respective objects")
                    .isEqualTo("demo.catalogue.CatalogueItemTo");

            final byte[] expectedBody = """
                    {"id":%d,"caption":"%s","description":"%s"}"""
                    .formatted(id, item.caption(), item.description())
                    .getBytes(StandardCharsets.UTF_8);
            assertThat(received.getBody())
                    .describedAs("The message body should match the expected pattern")
                    .isEqualTo(expectedBody);
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
