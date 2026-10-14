package demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
class ModelGatewayTest {

    private ModelGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = ModelGateway.create();
    }

    @Test
    void answerThePromptWithTheArtistName() {
        /* Given */
        final String prompt = """
                Who painted the Mona Lisa? \
                Please only answer with the artist's name.""";

        /* When */
        final String assistant = gateway.prompt(prompt);

        /* Given */
        assertThat(assistant)
                .describedAs("""
                        This is a direct question, and the model should respond \
                        with the artist's name. However, depending on the \
                        model's training, it may not follow the instruction \
                        precisely and could return additional information.""")
                .isEqualTo("Leonardo da Vinci");
    }
}
