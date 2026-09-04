package demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
class ModelGatewayIntegrationTest {

    private ModelGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = ModelGateway.create();
    }

    @Test
    void answerThePromptWithTheArtistName() throws InterruptedException {
        /* Given */
        final String prompt = """
                Who painted the Mona Lisa? \
                Please only answer with the artist's name.""";

        /* When */
        final CountDownLatch waitUntilCompletion = new CountDownLatch(1);
        final AtomicReference<String> assistant = new AtomicReference<>();
        gateway.prompt(prompt, response -> {
            assistant.set(response);
            waitUntilCompletion.countDown();
        });
        waitUntilCompletion.await();

        /* Given */
        assertThat(assistant.get())
                .describedAs("""
                        This is a very direct question and the model should answer \
                        with the artist's name""")
                .isEqualTo("Leonardo da Vinci");
    }
}
