package demo.quote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QuoteControllerTest {

    @MockitoBean
    private QuoteService service;

    @LocalServerPort
    private int port;

    private RestTestClient restTestClient;

    @BeforeEach
    public void setup() {
        restTestClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void returnNotFoundWhenNoQuotesAreFound() {
        /* Given */
        when(service.random()).thenReturn(Optional.empty());

        /* When */
        restTestClient.get()
                .uri("/quote/random")
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void returnFoundWithTheRandomQuote() {
        /* Given */
        final Quote quote = new Quote(1L, "Albert Einstein", "Learn from yesterday, live for today, hope for tomorrow. The important thing is not to stop questioning.");
        when(service.random()).thenReturn(Optional.of(quote));

        /* When */
        final Quote response = restTestClient.get()
                .uri("/quote/random")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Quote.class)
                .returnResult()
                .getResponseBody();

        /* Then */
        assertThat(response)
                .describedAs("Return the random quote as a JSON object")
                .isEqualTo(quote);
    }
}
