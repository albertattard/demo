package demo;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.Duration;
import java.util.Map;

import static java.util.Objects.requireNonNull;

final class ModelGateway {

    private final ChatModel chatModel;

    public static ModelGateway create() {
        return new ModelGateway();
    }

    /* Both parameters are plain strings and are vulnerable to injection attacks. */
    String translate(final String text, final Language language) {
        final String template = """
                Translate the following to {{language}}. Return only the translated version.
                
                {{text}}""";

        final PromptTemplate promptTemplate = PromptTemplate.from(template);
        final Map<String, Object> values = Map.of("language", language, "text", text);
        final Prompt prompt = promptTemplate.apply(values);

        /* Trim the output as it may start or ends with new-lines. */
        return prompt(prompt.text()).trim();
    }

    private String prompt(final String prompt) {
        requireNonNull(prompt);

        return this.chatModel.chat(prompt);
    }

    private ModelGateway() {
        chatModel = OllamaChatModel.builder()
                .modelName("llama2")
                .baseUrl("http://localhost:11434/")
                .timeout(Duration.ofSeconds(60))
                .build();
    }
}
