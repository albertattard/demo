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
                Q: When did Albert Einstein submit his paper on special relativity to the University of Zurich?
                A: July 30, 1905
                
                Q: When was the first Sputnik launched?
                A: October 4, 1957
                
                Q: When was the first version of Java released?
                A: May 23, 1995
                
                Q: When did humans first land on the Moon?""";

        /* When */
        final String assistant = gateway.prompt(prompt)
                .trim() /* It may start with a new-line or ends with a new-line*/;

        /* Given */
        assertThat(assistant)
                .describedAs("The model should reply with just the date in the same format as in the previous examples")
                .isEqualTo("A: July 20, 1969");
    }
}
