package demo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
@SpringBootTestWithTestProfile
class ModelGatewayIntegrationTest {

    @Autowired
    private ModelGateway gateway;

    @Test
    void interactWithTheModelUsingAudio() throws IOException {
        /* Given */
        final Path path = Path.of("./src/test/resources/audio/Prompt-1.mp3");

        /* When */
        final ModelGateway.PromptResult assistant = gateway.prompt(path);

        /* Then */
        assertThat(assistant.transcript())
                .isEqualTo("Humans first landed on the moon on July 20, 1969, during the Apollo 11 mission.");
        assertThat(assistant.audio())
                .isNotEmpty();

        writeToFile(assistant.audio());
        writeToFile(assistant.transcript());
    }

    private static void writeToFile(final byte[] audio) throws IOException {
        final Path audioFile = Path.of("target", "audio", "audio_test.mp3");
        Files.createDirectories(audioFile.getParent());
        Files.write(audioFile, audio);
    }

    private static void writeToFile(final String audio) throws IOException {
        final Path audioFile = Path.of("target", "audio", "audio_test.txt");
        Files.createDirectories(audioFile.getParent());
        Files.writeString(audioFile, audio);
    }
}
