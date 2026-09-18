package demo;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.Duration;
import java.util.List;

import static java.util.Objects.requireNonNull;

final class ModelGateway {

    private final ChatModel chatModel;
    private final ChatModel judgeModel;

    static ModelGateway create() {
        return new ModelGateway();
    }

    String chat(final String prompt) {
        requireNonNull(prompt);

        return chatModel.chat(UserMessage.from(prompt))
                .aiMessage()
                .text();
    }

    String judge(final String system, final String prompt) {
        requireNonNull(system);
        requireNonNull(prompt);

        return judgeModel.chat(List.of(SystemMessage.from(system), UserMessage.from(prompt)))
                .aiMessage()
                .text();
    }

    private ModelGateway() {
        this.chatModel = createModel("gemma:2b");
        this.judgeModel = createModel("llama3");
    }

    private static OllamaChatModel createModel(final String modelName) {
        return OllamaChatModel.builder()
                .modelName(modelName)
                .baseUrl("http://localhost:11434/")
                .timeout(Duration.ofSeconds(60))
                .temperature(0.0)
                .build();
    }
}
