package demo;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.Duration;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

final class ModelGateway {

    private final ChatModel chatModel;

    static ModelGateway create() {
        return new ModelGateway();
    }

    void system(final UUID sessionId, final String system) {
        requireNonNull(sessionId);
        requireNonNull(system);

        throw new UnsupportedOperationException("Not yet implemented");
    }

    String prompt(final UUID sessionId, final String prompt) {
        requireNonNull(sessionId);
        requireNonNull(prompt);

        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static MessageWindowChatMemory createMessageWindowChatMemory(final UUID sessionId) {
        return MessageWindowChatMemory.builder()
                .id(sessionId)
                .maxMessages(100)
                .chatMemoryStore(FileChatMemoryStore.create("./history"))
                .build();
    }

    private ModelGateway() {
        this.chatModel = OllamaChatModel.builder()
                .modelName("llama2")
                .baseUrl("http://localhost:11434/")
                .timeout(Duration.ofSeconds(60))
                .build();
    }
}
