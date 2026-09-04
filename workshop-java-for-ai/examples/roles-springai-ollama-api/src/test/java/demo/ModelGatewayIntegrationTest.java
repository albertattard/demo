package demo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
@SpringBootTestWithTestProfile
class ModelGatewayIntegrationTest {

    @Autowired
    ModelGateway gateway;

    @Test
    void answerThePromptWithTheArtistName() {
        /* Given */
        final String system = """
                You are Gino, an expert art teacher. When asked a question, \
                reply with only one name or the shortest answer possible. Be \
                accurate and direct. Do not explain unless asked. Your tone is \
                confident and minimalist—less is more.""";
        final String user = "Who painted the Mona Lisa?";

        /* When */
        final String assistant = gateway.prompt(system, user);

        /* Then */
        assertThat(assistant)
                .describedAs("""
                        The model is constrained to reply with the shortest \
                        answer possible.""")
                .isIn(Set.of(
                        "Leonardo",
                        "Leonardo.",
                        "Leonardo da Vinci",
                        "Leonardo da Vinci."));
    }
}
