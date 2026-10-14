package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ModelGateway {

    private final ChatClient chatClient;
    private final ModelPromptTemplates promptTemplates;

    public ModelGateway(
            final ChatClient.Builder builder,
            final ModelPromptTemplates promptTemplates) {
        this.chatClient = builder.build();
        this.promptTemplates = promptTemplates;
    }

    public RecommendationBrief recommend(final String userMessage) {
        return chatClient.prompt()
                .system(promptTemplates.recommendationSystemPrompt())
                .user(userMessage)
                .call()
                .entity(RecommendationBrief.class);
    }

    public DiyTaskInference inferTasks(final byte[] image, final MediaType mediaType) {
        return chatClient.prompt()
                .system(promptTemplates.taskInferenceSystemPrompt())
                .user(user -> user
                        .text(promptTemplates.taskInferenceUserPrompt())
                        .media(mediaType, new ByteArrayResource(image)))
                .call()
                .entity(DiyTaskInference.class);
    }

    public TaskInferenceSafetyReview judgeTaskChoices(final DiyTaskInference inference) {
        final String taskChoices = inference.taskChoices().stream()
                .map(choice -> "- " + choice)
                .collect(Collectors.joining("\n"));

        // A production application can use a separately configured, cheaper model for this judge call.
        return chatClient.prompt()
                .system(promptTemplates.taskJudgeSystemPrompt())
                .user(user -> user
                        .text(promptTemplates.taskJudgeUserPrompt())
                        .param("taskChoices", taskChoices))
                .call()
                .entity(TaskInferenceSafetyReview.class);
    }
}
