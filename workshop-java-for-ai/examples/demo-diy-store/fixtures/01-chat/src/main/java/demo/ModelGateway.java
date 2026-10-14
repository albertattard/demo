package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ModelGateway {

    private final ChatClient chatClient;
    private final Resource recommendationSystemPrompt;

    public ModelGateway(
            final ChatClient.Builder builder,
            @Value("classpath:prompts/recommendation-system.st") final Resource recommendationSystemPrompt) {
        this.chatClient = builder.build();
        this.recommendationSystemPrompt = recommendationSystemPrompt;
    }

    public RecommendationBrief recommend(final String userMessage) {
        return chatClient.prompt()
                .system(recommendationSystemPrompt)
                .user(userMessage)
                .call()
                .entity(RecommendationBrief.class);
    }
}
