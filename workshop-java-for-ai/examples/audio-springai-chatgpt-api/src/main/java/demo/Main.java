package demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.UncheckedIOException;
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
            final String text = readTextFromArgs(args);
            System.out.println("text> " + text);

            final byte[] synthesised = gateway.synthesis(text);
            final String fileName = "audio_%1$tY%1$tm%1$td_%1$tH%1$tM%1$tS.mp3".formatted(System.currentTimeMillis());
            final Path audioFile = writeToFile(synthesised, fileName);
            System.out.println("audio file> " + audioFile);

            final String transcribed = gateway.transcribe(Files.readAllBytes(audioFile));
            System.out.println("transcribed> " + transcribed);
        };
    }

    private static String readTextFromArgs(final String[] args) {
        return Arrays.stream(args)
                .findFirst()
                .orElse("""
                        The first successful manned Moon landing took place on July 20, 1969, during NASA's Apollo 11 \
                        mission.""");
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
