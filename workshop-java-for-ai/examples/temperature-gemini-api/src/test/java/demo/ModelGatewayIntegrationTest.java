package demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
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
    void returnTheSameOutputWhenTheTemperatureIsZero() {
        /* Given */
        final String model = "gemini-2.5-flash";
        final String prompt = "When did humans first land on the Moon?";
        final float temperature = 0F;

        /* When */
        final Set<String> assistant = new HashSet<>();
        assistant.add(gateway.prompt(model, prompt, temperature));
        assistant.add(gateway.prompt(model, prompt, temperature));

        /* Then */
        /* Given that the temperature is 0, the responses from both requests should be identical. However, this is not
           guaranteed, as the requests may be processed by different hardware, potentially yielding varying results. As
           a result, the test may fail. */
        assertThat(assistant)
                .describedAs("The model's output should be reproducible given the temperature is 0")
                .hasSize(1);
    }

    @Test
    void returnDifferentOutputWhenTheTemperatureIsGreaterThanZero() {
        /* Given */
        final String model = "gemini-2.5-flash";
        final String prompt = "When did humans first land on the Moon?";
        final float temperature = 2F;

        /* When */
        final Set<String> assistant = new HashSet<>();
        assistant.add(gateway.prompt(model, prompt, temperature));
        assistant.add(gateway.prompt(model, prompt, temperature));

        /* Then */
        /* Given that the temperature is 2, the responses from both requests should be different. However, this is not
           guaranteed, as the same set of words are picked to answer this specific question all the times. As a result,
           the test may fail. */
        assertThat(assistant)
                .describedAs("The model's output will vary given the temperature is 0")
                .hasSize(2);
    }

    @AfterEach
    void tearDown() {
        gateway.close();
    }
}
