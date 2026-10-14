package demo;

import com.cohere.api.Cohere;
import com.cohere.api.requests.ChatRequest;
import com.cohere.api.types.ChatRequestPromptTruncation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class ModelGateway {

    private final Cohere client;

    static ModelGateway create() {
        return new ModelGateway();
    }

    String prompt(final String prompt) {
        requireNonNull(prompt);

        final ChatRequest request = ChatRequest.builder()
                .message(prompt)
                .model("command-a-03-2025")
                .temperature(0.0F)
                .promptTruncation(ChatRequestPromptTruncation.AUTO)
                .build();

        return client.chat(request)
                .getText();
    }

    private ModelGateway() {
        this.client = Cohere.builder()
                .token(getCohereApiKey())
                .clientName("demo")
                .build();
    }

    private static String getCohereApiKey() {
        return Stream.of(Optional.ofNullable(System.getProperty("cohere.api.key")),
                        Optional.ofNullable(System.getenv("COHERE_API_KEY")),
                        readCohereApiKeyFromWellKnownLocation())
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cohere API Key missing"));
    }

    private static Optional<String> readCohereApiKeyFromWellKnownLocation() {
        final Path file = Path.of(System.getProperty("user.home"), ".cohere", "api.key");
        if (Files.isRegularFile(file)) {
            try (Stream<String> lines = Files.lines(file)) {
                return lines.filter(l -> !l.isBlank())
                        .filter(l -> !l.startsWith("#"))
                        .findFirst();
            } catch (final IOException e) {
                throw new UncheckedIOException("Failed to read the Cohere API key from file " + file, e);
            }
        }

        return Optional.empty();
    }
}
