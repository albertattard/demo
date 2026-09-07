package demo.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoginFailureHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void jerryCanSignInWithTheWorkshopPassword() throws Exception {
        mockMvc.perform(formLogin().user("jerry").password("testing"))
                .andExpect(authenticated().withUsername("jerry"));
    }

    @Test
    void failedSignInUsesAGenericVisibleMessage() throws Exception {
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "nobody")
                        .param("password", "wrong"))
                .andExpect(status().isUnauthorized());
    }
}
