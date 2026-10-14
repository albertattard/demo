package demo;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static java.util.Objects.requireNonNull;

final class ModelGateway {

    private final ChatModel chatModel;

    static ModelGateway create() {
        return new ModelGateway();
    }

    String describeImage(final Path path) {
        requireExistingFile(path);

        final UserMessage message = UserMessage.builder()
                .addContent(TextContent.from("Describe what you see in the attached image in a sentence or two."))
                .addContent(ImageContent.from(Image.builder().url(path.toUri()).build()))
                .build();

        return chatModel.chat(message)
                .aiMessage()
                .text()
                .trim();
    }

    private static void requireExistingFile(final Path path) {
        requireNonNull(path);
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("The path '" + path + "' is not a regular file");
        }
    }

    private ModelGateway() {
        this.chatModel = OllamaChatModel.builder()
                .modelName("llava:7b")
                .baseUrl("http://localhost:11434/")
                .timeout(Duration.ofSeconds(60))
                .temperature(0.0)
                .build();
    }
}
