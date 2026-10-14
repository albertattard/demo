package demo;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.openai.models.ChatModel.GPT_4_1_MINI;
import static java.util.Objects.requireNonNull;

final class ModelGateway implements AutoCloseable {

    private final OpenAIClient client;

    static ModelGateway create() {
        return new ModelGateway();
    }

    String prompt(final String prompt) {
        requireNonNull(prompt);

        final ResponseCreateParams params = ResponseCreateParams.builder()
                .input(prompt)
                .model(GPT_4_1_MINI)
                .build();

        final Response response = client.responses().create(params);
        return response.output().stream()
                .filter(ResponseOutputItem::isMessage)
                .flatMap(item -> item.asMessage().content().stream())
                .filter(ResponseOutputMessage.Content::isOutputText)
                .map(c -> c.asOutputText().text())
                .collect(Collectors.joining("\n"));
    }

    private ModelGateway() {
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(getChatGptApiKey())
                .build();
    }

    @Override
    public void close() {
        client.close();
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
