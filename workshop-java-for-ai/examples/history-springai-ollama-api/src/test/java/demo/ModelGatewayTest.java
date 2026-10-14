package demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@EnableWireMock(@ConfigureWireMock(baseUrlProperties = "ollama.base.url"))
@SpringBootTestWithTestProfile(properties = "spring.ai.ollama.base-url=${ollama.base.url}")
class ModelGatewayTest {

    @Autowired
    private ModelGateway gateway;

    private static final String RESPONSE = "This is a mocked response";

    @BeforeEach
    public void setup() {
        WireMock.reset();
        WireMock.stubFor(post(urlPathEqualTo("/api/chat"))
                .willReturn(okJson("""
                        {
                          "model": "llama3",
                          "created_at": "2077-04-27T12:34:56.789000000Z",
                          "message": {
                            "role": "assistant",
                            "content": "${content}"
                          },
                          "done": true,
                          "done_reason": "stop",
                          "total_duration": 8809883207,
                          "load_duration": 99296690,
                          "prompt_eval_count": 30,
                          "prompt_eval_duration": 531322873,
                          "eval_count": 104,
                          "eval_duration": 8109764913
                        }""".replace("${content}", RESPONSE))));
    }

    @Test
    void includeAllMessagesFromTheSameSession() throws IOException {
        /* Given */
        final UUID sessionId = UUID.randomUUID();
        final String prompt1 = "Describe the moment Neil Armstrong first steps onto the Moon in 1969 in a sentence or two.";
        final String prompt2 = "Rewrite that moment from the perspective of someone watching it live on a black-and-white TV in a sentence or two.";

        /* When */
        gateway.prompt(sessionId, prompt1);
        gateway.prompt(sessionId, prompt2);

        /* Then */
        final List<ServeEvent> calls = WireMock.getAllServeEvents();
        assertThat(calls)
                .describedAs("Two requests were made to the model")
                .hasSize(2);

        final ServeEvent lastRequest = calls.getFirst(); /* The last request */
        final JsonNode messages = new ObjectMapper().readTree(lastRequest.getRequest().getBodyAsString()).get("messages");
        assertThat(messages.isArray())
                .isTrue();
        assertThat(messages.size())
                .describedAs("The previous conversation should be included as these belong to the same session")
                .isEqualTo(3);
        assertMessage(messages.get(0), "user", prompt1);
        assertMessage(messages.get(1), "assistant", RESPONSE);
        assertMessage(messages.get(2), "user", prompt2);
    }

    @Test
    void onlyIncludeMessagesFromTheSameSession() throws JsonProcessingException {
        /* Given */
        final String prompt1 = "Describe the moment Neil Armstrong first steps onto the Moon in 1969 in a sentence or two.";
        final String prompt2 = "Rewrite that moment from the perspective of someone watching it live on a black-and-white TV in a sentence or two.";

        /* When */
        gateway.prompt(UUID.randomUUID(), prompt1);
        gateway.prompt(UUID.randomUUID(), prompt2);

        /* Then */
        final List<ServeEvent> calls = WireMock.getAllServeEvents();
        assertThat(calls)
                .describedAs("Two requests where made to the model")
                .hasSize(2);

        final ServeEvent lastRequest = calls.getFirst(); /* The last request */
        final JsonNode messages = new ObjectMapper().readTree(lastRequest.getRequest().getBodyAsString()).get("messages");
        assertThat(messages.isArray())
                .isTrue();
        assertThat(messages.size())
                .describedAs("The previous conversation should not be included as these belong to different sessions")
                .isEqualTo(1);
        assertMessage(messages.get(0), "user", prompt2);
    }

    private static void assertMessage(final JsonNode message, final String role, final String content) {
        assertThat(message.has("role")).isTrue();
        assertThat(message.get("role").isTextual()).isTrue();
        assertThat(message.get("role").asText()).isEqualTo(role);
        assertThat(message.has("content")).isTrue();
        assertThat(message.get("content").isTextual()).isTrue();
        assertThat(message.get("content").asText()).isEqualTo(content);
    }
}
