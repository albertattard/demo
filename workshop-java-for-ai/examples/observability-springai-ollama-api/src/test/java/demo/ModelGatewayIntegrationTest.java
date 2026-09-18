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
    private ModelGateway gateway;

    @Test
    void answerThePromptWithTheArtistName() {
        /* Given */
        final String prompt = """
                Who painted the Mona Lisa? \
                Please only answer with the artist's name.""";

        /* When */
        final Assistant assistant = gateway.prompt(Prompt.of(prompt));

        /* Then */
        assertThat(assistant.assistant())
                .describedAs("""
                        This is a direct question, and the model should respond \
                        with the artist's name. However, depending on the \
                        model's training, it may not follow the instruction \
                        precisely and could return additional information.""")
                .isIn(Set.of("Leonardo",
                        "Leonardo da Vinci.",
                        "Leonardo da Vinci"));
    }
}
