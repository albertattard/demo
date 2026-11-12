package demo.quote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@AutoConfigureMockMvc
@SpringBootTest
class QuoteControllerTest {

    @MockitoBean
    private QuoteService service;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnNotFoundWhenNoQuotesAreFound() throws Exception {
        // Given
        when(service.random()).thenReturn(Optional.empty());

        /* When */
        final MvcResult response = makeGetRandomQuoteRequest();

        /* Then */
        assertThat(response.getResponse().getStatus())
                .describedAs("Return HTTP NOT_FOUND when a random quote is not found")
                .isEqualTo(HttpStatus.NOT_FOUND.value());
        verify(service).random();
    }

    @Test
    void returnFoundWithTheRandomQuote() throws Exception {
        /* Given */
        final Quote quote = new Quote(1L, "Albert Einstein", "Learn from yesterday, live for today, hope for tomorrow. The important thing is not to stop questioning.");
        when(service.random()).thenReturn(Optional.of(quote));

        /* When */
        final MvcResult response = makeGetRandomQuoteRequest();

        /* Then */
        assertThat(response.getResponse().getStatus())
                .describedAs("Return HTTP OK when a random quote is found")
                .isEqualTo(HttpStatus.OK.value());
        assertThat(response.getResponse().getContentAsString())
                .describedAs("Return the random quote as a JSON object")
                .contains("\"id\":1")
                .contains("\"author\":\"Albert Einstein\"")
                .contains("\"quote\":\"Learn from yesterday, live for today, hope for tomorrow. The important thing is not to stop questioning.\"");
        verify(service).random();
    }

    private MvcResult makeGetRandomQuoteRequest() throws Exception {
        return mockMvc.perform(get("/quote/random"))
                .andReturn();
    }
}
