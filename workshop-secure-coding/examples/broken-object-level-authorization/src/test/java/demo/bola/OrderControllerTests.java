package demo.bola;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void aliceCanAuthenticateWithTheSeededBcryptPassword() throws Exception {
        mockMvc.perform(formLogin().user("alice").password("alice-password"))
                .andExpect(authenticated().withUsername("alice"));
    }

    @Test
    void homePageShowsOnlyTheAuthenticatedUsersOrders() throws Exception {
        mockMvc.perform(get("/").with(user("alice")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alice: Notebook")))
                .andExpect(content().string(containsString("Alice: Pencil")))
                .andExpect(content().string(not(containsString("Bob: Camera"))));
    }

    @Test
    void aliceCanReadAndChangeHerOrder() throws Exception {
        mockMvc.perform(get("/order/1001").with(user("alice")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alice: Notebook")));

        mockMvc.perform(post("/order/1001")
                        .with(user("alice"))
                        .with(csrf())
                        .param("description", "Alice changed her notebook order"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/order/1001").with(user("alice")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alice changed her notebook order")));
    }

    @Test
    void missingOrderUsesTheCustomErrorPage() throws Exception {
        mockMvc.perform(get("/order/9999").with(user("alice")))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("We could not find that order")));
    }
}
