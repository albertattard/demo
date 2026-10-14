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
    void translateTextToGerman() {
        /* Given */
        final String text = "The apple is on the table.";
        final Language language = Language.GERMAN;

        /* When */
        final String translated = gateway.translate(text, language);

        /* Then */
        /* There is more than one possible translation */
        assertThat(translated)
                .describedAs("Translate the given simple text")
                .isIn(Set.of(
                        "Der Apfel liegt auf dem Tisch.",
                        "Der Apfel ist auf dem Tisch."));
    }
}
