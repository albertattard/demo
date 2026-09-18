package demo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class ModelGateway implements AutoCloseable {

    private final HttpClient client;

    static ModelGateway create() {
        return new ModelGateway();
    }

    String prompt(final String model, final String prompt, final double temperature) {
        requireNonNull(model);
        requireNonNull(prompt);
        requireValidTemperature(temperature);

        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent"))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", getGeminiApiKey())
                    .POST(createBodyPublisher(prompt, temperature))
                    .build();

            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Received an unexpected response (" + response.statusCode() + ") from server");
            }

            return extractAnswer(response.body())
                    .orElseThrow(() -> new RuntimeException("Failed to extract response from AI!"));
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to make request", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for response", e);
        }
    }

    private static HttpRequest.BodyPublisher createBodyPublisher(final String prompt, final double temperature) {
        final String requestBody = """
                {
                  "contents": [
                    {
                      "parts": [
                        {
                          "text": "${prompt}"
                        }
                      ]
                    }
                  ],
                  "generationConfig": {
                    "temperature": ${temperature}
                  }
                }"""
                .replace("${prompt}", prompt)
                .replace("${temperature}", String.valueOf(temperature));

        return HttpRequest.BodyPublishers.ofString(requestBody);
    }

    private static Optional<String> extractAnswer(final String response) {
        final String marker = "\"text\": \"";
        final int markerStartIndex = response.indexOf(marker);

        if (markerStartIndex == -1) {
            return Optional.empty();
        }

        final int answerStartIndex = markerStartIndex + marker.length();
        final String substring = response.substring(answerStartIndex);
        final int answerEndIndex = substring.indexOf('"');
        if (answerEndIndex == -1) {
            return Optional.empty();
        }

        return Optional.of(substring.substring(0, answerEndIndex));
    }

    @Override
    public void close() {
        client.close();
    }

    private ModelGateway() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
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
