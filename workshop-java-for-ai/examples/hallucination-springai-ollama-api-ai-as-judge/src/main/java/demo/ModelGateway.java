package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

@Service
class ModelGateway {

    private final ChatClient chatClient;
    private final ChatClient judgeClient;

    ModelGateway(final @Qualifier("chatClient") ChatClient chatClient,
                 final @Qualifier("judgeClient") ChatClient judgeClient) {
        this.chatClient = chatClient;
        this.judgeClient = judgeClient;
    }

    String chat(final String prompt) {
        requireNonNull(prompt);

        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    String judge(final String system, final String prompt) {
        requireNonNull(prompt);

        return judgeClient.prompt(prompt)
                .system(system)
                .call()
                .content();
    }
}
