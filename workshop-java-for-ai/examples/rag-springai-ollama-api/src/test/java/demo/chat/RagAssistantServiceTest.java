package demo.chat;

import demo.chat.spring.RagAssistantService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
@SpringBootTestWithTestProfile
class RagAssistantServiceTest {

    @Autowired
    private RagAssistantService service;

    @Test
    void recommendMakeTheRightRecommendation() {
        final List<DialogueMessage> history = List.of(DialogueMessage.user("skiing"),
                DialogueMessage.assistant("What a wonderful topic! I think I can help with that. The Alps offer some of Europe's top ski resorts, including French Alps, Austria, Switzerland, and Italy. If you're interested in skiing, this 7-Day Adventure & Extreme Sports - Alps package seems like an excellent choice, offering beginner-friendly lessons and kid-focused snow activities. In fact, the Best Travel Season for this package is December to March, which coincides with peak skiing season. Would you like me to tell you more about it?"));
        final AssistantResponse reply = service.reply(history, "how much would that cost?");

        /* TODO: The currency may change in different locales */
        assertThat(reply.text())
                // match 2350 with optional separators (comma, dot, or space)
                .matches(".*\\b(€)?2[ ,.]?350\\b.*")
                .containsIgnoringCase("double occupancy");

        assertThat(reply.references().stream().map(AssistantReference::slug).toList())
                .hasSize(1)
                .contains("7-day-adventure-extreme-sports-alps");
    }
}
