package demo.catalogue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogueItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void allowsAnonymousAccessToTheCatalogue() throws Exception {
        mockMvc.perform(get("/catalogue/item/1").with(remoteAddress("198.51.100.10")))
                .andExpect(status().isOk());
    }

    @Test
    void sharesTheQuotaForRequestsFromTheSameSourceAddress() throws Exception {
        final RequestPostProcessor sharedAddress = remoteAddress("198.51.100.11");
        for (int attempt = 0; attempt < 20; attempt++) {
            mockMvc.perform(get("/catalogue/item/1").with(sharedAddress))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/catalogue/item/1").with(sharedAddress))
                .andExpect(status().isTooManyRequests());
        mockMvc.perform(get("/catalogue/item/1").with(remoteAddress("198.51.100.12")))
                .andExpect(status().isOk());
    }

    private static RequestPostProcessor remoteAddress(final String address) {
        return request -> {
            request.setRemoteAddr(address);
            return request;
        };
    }
}
