package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

@Service
class ModelGateway {

    private final ChatClient client;

    private ModelGateway(final ChatClient.Builder builder) {
        this.client = builder.build();
    }

    String prompt(final String system, final String prompt) {
        requireNonNull(system);
        requireNonNull(prompt);

        return client.prompt(prompt)
                .system(system)
                .call()
                .content();
    }
}
