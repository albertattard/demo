package demo;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
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

        final List<ChatMessage> messages = List.of(/* TODO: Add the messages */);

        final ChatResponse response = chatModel.chat(messages);
        return response.aiMessage().text();
    }

    private ModelGateway() {
        this.chatModel = OllamaChatModel.builder()
                .modelName("llama2")
                .baseUrl("http://localhost:11434/")
                .timeout(Duration.ofSeconds(60))
                .build();
    }
}
