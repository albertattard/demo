package demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void submittedPlainTextIsShownInTheResult() throws Exception {
        mockMvc.perform(post("/").param("message", "Welcome, attendee"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "<div class=\"border p-3\">Welcome, attendee</div>")));
    }

}
