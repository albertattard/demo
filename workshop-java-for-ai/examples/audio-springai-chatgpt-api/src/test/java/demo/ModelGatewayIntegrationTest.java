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
    ModelGateway gateway;

    @Test
    void synthesisAndTranscribe() throws IOException {
        /* Given */
        final String text = "The first person to walk on the Moon was Neil Armstrong.";

        /* When */
        final byte[] synthesised = gateway.synthesis(text);

        /* Then */
        assertThat(synthesised)
                .isNotEmpty();

        final Path audioFile = writeToFile(synthesised);

        final String transcribed = gateway.transcribe(Files.readAllBytes(audioFile));
        assertThat(transcribed)
                .isEqualToIgnoringCase(text);
    }

    private static Path writeToFile(final byte[] synthesised) throws IOException {
        final Path audioFile = Path.of("target", "audio", "audio_test.mp3");
        Files.createDirectories(audioFile.getParent());
        Files.write(audioFile, synthesised);
        return audioFile;
    }
}
