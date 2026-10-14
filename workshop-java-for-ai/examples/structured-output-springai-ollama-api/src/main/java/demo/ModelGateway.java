package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Service
class ModelGateway {

    private final ChatClient client;
    private final Resource systemTemplate;

    ModelGateway(final ChatClient.Builder builder,
                 @Value("classpath:/prompts/system.st") final Resource systemTemplate) {
        this.client = builder.build();
        this.systemTemplate = systemTemplate;
    }

    Optional<LocalDateTime> extractDateTime(final String text) {
        requireNonNull(text);

        final ResponseEntity<ChatResponse, LocalDateTime> response = client.prompt(text)
                .system(spec -> spec.text(systemTemplate)
                        .param("now", LocalDateTime.now())
                        .param("timezone", ZoneId.systemDefault()))
                .call()
                .responseEntity(LocalDateTime.class);

        return Optional.ofNullable(response.entity());
    }
}
