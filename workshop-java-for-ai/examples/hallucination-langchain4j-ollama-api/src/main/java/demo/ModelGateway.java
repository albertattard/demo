package demo;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.Duration;

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
        chatModel = OllamaChatModel.builder()
                .modelName("gemma:2b")
                .baseUrl("http://localhost:11434/")
                .timeout(Duration.ofSeconds(60))
                .temperature(0.0)
                .build();
    }
}
