package demo;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class ModelGateway {

    private final ChatModel chatModel;

    static ModelGateway create() {
        return new ModelGateway();
    }

    String prompt(final String prompt) {
        requireNonNull(prompt);

        return this.chatModel.chat(prompt);
    }

    private ModelGateway() {
        this.chatModel = GoogleAiGeminiChatModel.builder()
                .modelName("gemini-2.5-flash")
                .apiKey(getGeminiApiKey())
                .timeout(Duration.ofSeconds(60))
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
