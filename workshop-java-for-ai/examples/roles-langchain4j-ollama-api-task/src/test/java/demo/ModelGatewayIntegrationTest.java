package demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
class ModelGatewayIntegrationTest {

    private ModelGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = ModelGateway.create();
    }

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
