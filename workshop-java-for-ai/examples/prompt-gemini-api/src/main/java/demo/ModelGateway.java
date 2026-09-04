package demo;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class ModelGateway implements AutoCloseable {

    private final Client client;

    static ModelGateway create() {
        return new ModelGateway();
    }

    String prompt(final String prompt) {
        requireNonNull(prompt);

        final String model = "gemini-2.5-flash";

        final GenerateContentConfig config = GenerateContentConfig.builder()
                .temperature(0.0F)
                .build();

        return client.models
                .generateContent(model, prompt, config)
                .text();
    }

    @Override
    public void close() {
        client.close();
    }

    private ModelGateway() {
        this.client = Client.builder()
                .apiKey(getGeminiApiKey())
                .build();
    }

    private static String getGeminiApiKey() {
        return Stream.of(Optional.ofNullable(System.getProperty("gemini.api.key")),
                        Optional.ofNullable(System.getenv("GEMINI_API_KEY")),
                        readGeminiApiKeyFromWellKnownLocation())
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Gemini API Key missing"));
    }

    private static Optional<String> readGeminiApiKeyFromWellKnownLocation() {
        final Path file = Path.of(System.getProperty("user.home"), ".gemini", "api.key");
        if (Files.isRegularFile(file)) {
            try (Stream<String> lines = Files.lines(file)) {
                return lines.filter(l -> !l.isBlank())
                        .filter(l -> !l.startsWith("#"))
                        .findFirst();
            } catch (final IOException e) {
                throw new UncheckedIOException("Failed to read the Gemini API key from file " + file, e);
            }
        }

        return Optional.empty();
    }
}
