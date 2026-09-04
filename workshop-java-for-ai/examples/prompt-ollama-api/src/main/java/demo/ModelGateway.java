package demo;

import io.github.ollama4j.Ollama;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;

import static java.util.Objects.requireNonNull;

final class ModelGateway {

    private final Ollama ollama;

    static ModelGateway create() {
        return new ModelGateway();
    }

    String prompt(final String prompt) {
        requireNonNull(prompt);

        final OllamaChatRequest request = OllamaChatRequest.builder()
                .withModel("llama2")
                .withMessage(OllamaChatMessageRole.USER, prompt)
                .build();

        try {
            return ollama.chat(request, null)
                    .getResponseModel()
                    .getMessage()
                    .getResponse();
        } catch (final OllamaException e) {
            throw new RuntimeException("Failed to make request to the model", e);
        }
    }

    private ModelGateway() {
        ollama = new Ollama();
        ollama.setRequestTimeoutSeconds(60);
    }
}
