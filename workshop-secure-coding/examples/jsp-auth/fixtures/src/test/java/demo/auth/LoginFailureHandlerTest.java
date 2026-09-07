package demo.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestPostProcessors.csrf;

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
    void knownAndUnknownAccountFailuresAreExternallyIdentical() throws Exception {
        final MockHttpServletResponse knownAccount = failedLogin("jerry");
        final MockHttpServletResponse unknownAccount = failedLogin("nobody");

        assertThat(knownAccount.getStatus()).isEqualTo(unknownAccount.getStatus());
        assertThat(knownAccount.getContentType()).isEqualTo(unknownAccount.getContentType());
        assertThat(knownAccount.getContentAsByteArray())
                .isEqualTo(unknownAccount.getContentAsByteArray());
    }

    private MockHttpServletResponse failedLogin(final String username) throws Exception {
        return mockMvc.perform(post("/login").with(csrf())
                        .param("username", username)
                        .param("password", "wrong"))
                .andReturn()
                .getResponse();
    }
}
