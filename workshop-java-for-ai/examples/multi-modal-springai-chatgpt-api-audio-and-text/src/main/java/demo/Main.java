package demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@SpringBootApplication
public class Main {

    public static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    @NotTestProfile
    CommandLineRunner createRunner(final ModelGateway gateway) {
        return args -> {
            final Path audio = readAudioPathFromArgs(args);
            System.out.println("prompt> " + audio);

            final ModelGateway.PromptResult assistant = gateway.prompt(audio);
            final String fileName = "audio_%1$tY%1$tm%1$td_%1$tH%1$tM%1$tS".formatted(System.currentTimeMillis());
            final Path audioFile = writeToFile(assistant.audio(), fileName + ".mp3");
            final Path transcriptFile = writeToFile(assistant.transcript().getBytes(StandardCharsets.UTF_8), fileName + ".txt");
            System.out.println("audio file> " + audioFile);
            System.out.println("transcript> " + assistant.transcript());
            System.out.println("transcript file> " + transcriptFile);
        };
    }

    private static Path readAudioPathFromArgs(final String[] args) {
        return Arrays.stream(args)
                .map(Path::of)
                .findFirst()
                .orElse(Path.of("assets", "audio", "Prompt-1.mp3"));
    }

    private static Path writeToFile(final byte[] bytes, final String fileName) {
        final Path path = Path.of("target", "audio", fileName);
        try {
            final Path parent = path.getParent();
            if (!Files.isDirectory(path)) {
                Files.createDirectories(parent);
            }

            Files.write(path, bytes);
            return path;
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to write audio to file: " + path, e);
        }
    }
}
