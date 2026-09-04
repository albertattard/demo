package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

@Service
class ModelGateway {

    private final ChatClient client;

    ModelGateway(final ChatClient.Builder builder,
                 final ToolCallbackProvider tools) {
        this.client = builder
                .defaultToolCallbacks(tools)
                .build();
    }

    String prompt(final String prompt) {
        requireNonNull(prompt);

        return client.prompt(prompt)
                .call()
                .content();
    }
}
