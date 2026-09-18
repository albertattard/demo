package demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

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
        final String prompt = "When did humans first land on the Moon?";

        /* When */
        final String assistant = gateway.prompt(prompt);

        /* Then */
        assertThat(assistant)
                .describedAs("This model doesn't believe that humans landed on the moon.")
                .isIn(Set.of("There is no evidence to support the claim that humans have ever landed on the Moon."));
    }
}
