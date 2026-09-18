package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Service
class ModelGateway {

    private final ChatClient client;

    ModelGateway(final ChatClient.Builder builder) {
        final CallAdvisor safeGuardAdvisor = SafeGuardAdvisor.builder()
                /* Any prompt containing this exact string will be blocked. This rule is case-sensitive and applies even
                   if the string appears as part of a longer word, such as “metapolitics”. */
                .sensitiveWords(List.of("politics"))
                .failureResponse("I’m sorry, but I’m unable to discuss political matters.")
                .build();

        this.client = builder
                .defaultAdvisors(safeGuardAdvisor)
                .build();
    }

    String prompt(final String prompt) {
        requireNonNull(prompt);

        return client.prompt(prompt)
                .call()
                .content();
    }
}
