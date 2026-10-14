package demo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
@SpringBootTestWithTestProfile
class ModelGatewayTest {

    @Autowired
    ModelGateway gateway;

    @Test
    void answerThePromptWithTheArtistName() {
        /* Given */
        final String text = "The apple is on the table.";
        final Language language = Language.GERMAN;

        /* When */
        final String translated = gateway.translate(text, language);

        /* Then */
        /* There is more than one possible translation */
        assertThat(translated)
                .describedAs("Translate the given simple text")
                .isIn(Set.of("Der Apfel liegt auf dem Tisch.",
                        "Der Apfel ist auf dem Tisch."));
    }
}
