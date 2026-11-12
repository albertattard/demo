package demo.quote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class SlowQuoteControllerTest {

    @MockitoBean
    private QuoteService service;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsQuoteWithoutDelayWhenRequested() throws Exception {
        when(service.random()).thenReturn(Optional.of(new Quote(1L, "Author", "Quote")));

        mockMvc.perform(get("/quote/slow?seconds=0"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsDelayLongerThanTheDemonstrationLimit() throws Exception {
        mockMvc.perform(get("/quote/slow?seconds=31"))
                .andExpect(status().isBadRequest());
    }
}
