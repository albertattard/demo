package demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
        final String prompt = """
                Who painted the Mona Lisa? \
                Please only answer with the artist's name.""";

        /* When */
        final String assistant = gateway.prompt(prompt);

        /* Then */
        assertThat(assistant)
                .describedAs("""
                        This is a very direct question and the model should answer \
                        with the artist's name""")
                .isEqualTo("Leonardo da Vinci");
    }
}
