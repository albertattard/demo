package demo;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.Duration;
import java.util.List;

import static java.util.Objects.requireNonNull;

final class ModelGateway {

    private final ChatModel chatModel;

    static ModelGateway create() {
        return new ModelGateway();
    }

    String prompt(final String system, final String user) {
        requireNonNull(system);
        requireNonNull(user);

        final List<ChatMessage> messages = List.of(
                SystemMessage.from(system),
                UserMessage.from(user));

        return chatModel.chat(messages)
                .aiMessage()
                .text();
    }

    private ModelGateway() {
        this.chatModel = OllamaChatModel.builder()
                .modelName("llama2")
                .baseUrl("http://localhost:11434/")
                .timeout(Duration.ofSeconds(60))
                .temperature(0.0)
                .build();
    }
}
