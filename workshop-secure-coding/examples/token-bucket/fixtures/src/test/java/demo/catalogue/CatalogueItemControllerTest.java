package demo.catalogue;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CatalogueItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void requiresAnAuthenticatedClient() throws Exception {
        mockMvc.perform(get("/catalogue/item/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void limitsEachAuthenticatedClientIndependently() throws Exception {
        for (int attempt = 0; attempt < 20; attempt++) {
            mockMvc.perform(get("/catalogue/item/1").with(httpBasic("alice", "alice-password")))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/catalogue/item/1").with(httpBasic("alice", "alice-password")))
                .andExpect(status().isTooManyRequests());
        mockMvc.perform(get("/catalogue/item/1").with(httpBasic("bob", "bob-password")))
                .andExpect(status().isOk());
    }

    @Test
    void chargesBulkReadsMoreThanSimpleReads() throws Exception {
        mockMvc.perform(get("/catalogue/item/all/1").with(httpBasic("alice", "alice-password")))
                .andExpect(status().isOk());

        for (int attempt = 0; attempt < 15; attempt++) {
            mockMvc.perform(get("/catalogue/item/1").with(httpBasic("alice", "alice-password")))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(get("/catalogue/item/1").with(httpBasic("alice", "alice-password")))
                .andExpect(status().isTooManyRequests());
    }
}
