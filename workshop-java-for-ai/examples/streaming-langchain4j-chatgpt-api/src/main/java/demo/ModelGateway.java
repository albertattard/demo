package demo;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_5_NANO;
import static java.util.Objects.requireNonNull;

final class ModelGateway {

    private final StreamingChatModel chatModel;

    static ModelGateway create() {
        return new ModelGateway();
    }

    void prompt(final String prompt, final PromptCallback callback) {
        requireNonNull(prompt);

        chatModel.chat(prompt, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(final String token) {
                callback.onToken(token);
            }

            @Override
            public void onCompleteResponse(final ChatResponse chatResponse) {
                callback.onComplete(chatResponse.aiMessage().text());
            }

            @Override
            public void onError(final Throwable throwable) {
                callback.onError(throwable);
            }
        });
    }

    @FunctionalInterface
    interface PromptCallback {
        default void onToken(final String token) {}

        void onComplete(String response);

        default void onError(final Throwable throwable) {
            throw new RuntimeException("Failed to prompt the model", throwable);
        }
    }

    private ModelGateway() {
        this.chatModel = OpenAiStreamingChatModel.builder()
                .modelName(GPT_5_NANO)
                .apiKey(getChatGptApiKey())
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    private static String getChatGptApiKey() {
        return Stream.of(Optional.ofNullable(System.getProperty("chatgpt.api.key")),
                        Optional.ofNullable(System.getenv("CHATGPT_API_KEY")),
                        readChatGptApiKeyFromWellKnownLocation())
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("ChatGPT API Key missing"));
    }

    private static Optional<String> readChatGptApiKeyFromWellKnownLocation() {
        final Path file = Path.of(System.getProperty("user.home"), ".chatgpt", "api.key");
        if (Files.isRegularFile(file)) {
            try (Stream<String> lines = Files.lines(file)) {
                return lines.filter(l -> !l.isBlank())
                        .filter(l -> !l.startsWith("#"))
                        .findFirst();
            } catch (final IOException e) {
                throw new UncheckedIOException("Failed to read the ChatGPT API key from file " + file, e);
            }
        }

        return Optional.empty();
    }
}
