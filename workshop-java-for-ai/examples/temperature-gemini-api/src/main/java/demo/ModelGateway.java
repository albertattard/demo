package demo;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class ModelGateway implements AutoCloseable {

    private Client client;

    static ModelGateway create() {
        return new ModelGateway();
    }

    String prompt(final String model, final String prompt, final float temperature) {
        requireNonNull(model);
        requireNonNull(prompt);
        requireValidTemperature(temperature);

        final GenerateContentConfig config = GenerateContentConfig.builder()
                .temperature(temperature)
                .build();

        final GenerateContentResponse response =
                client.models.generateContent(model, prompt, config);

        return response.text();
    }

    @Override
    public void close() {
        this.client.close();
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

    private static void requireValidTemperature(final double temperature) {
        if (temperature < 0 || temperature > 2) {
            throw new IllegalArgumentException("The temperature must be between 0 and 2 (both inclusive)");
        }
    }
}
