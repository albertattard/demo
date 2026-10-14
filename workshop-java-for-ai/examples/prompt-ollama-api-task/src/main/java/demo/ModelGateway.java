package demo;

import io.github.ollama4j.Ollama;

import static java.util.Objects.requireNonNull;

final class ModelGateway {

    private final Ollama ollama;

    static ModelGateway create() {
        return new ModelGateway();
    }

    String prompt(final String prompt) {
        requireNonNull(prompt);

        throw new UnsupportedOperationException("Please implement this method");
    }

    private ModelGateway() {
        ollama = new Ollama();
        ollama.setRequestTimeoutSeconds(60);
    }
}
