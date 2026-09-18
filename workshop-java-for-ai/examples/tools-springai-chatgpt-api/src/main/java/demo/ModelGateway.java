package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

@Service
class ModelGateway {

    private final ChatClient client;
    private final Resource prompt;

    ModelGateway(final ChatClient.Builder builder,
                 final TimeTools timeTools,
                 @Value("classpath:/prompts/time-at-city.st") final Resource prompt) {
        this.client = builder
                .defaultTools(timeTools)
                .build();
        this.prompt = prompt;
    }

    String timeIn(final String city) {
        requireNonNull(city);

        return client.prompt()
                .user(spec -> spec.text(prompt).param("city", city))
                .call()
                .content();
    }
}
