package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModelGateway {

    private final ChatClient chatClient;
    private final ModelPromptTemplates promptTemplates;
    private final LocalStoreTools localStoreTools;

    public ModelGateway(
            final ChatClient.Builder builder,
            final ModelPromptTemplates promptTemplates,
            final LocalStoreTools localStoreTools) {
        this.chatClient = builder.build();
        this.promptTemplates = promptTemplates;
        this.localStoreTools = localStoreTools;
    }

    public RetrievalQueries createRetrievalQueries(final String userMessage) {
        return chatClient.prompt()
                .system(promptTemplates.retrievalQueriesSystemPrompt())
                .user(userMessage)
                .call()
                .entity(RetrievalQueries.class);
    }

    public ProductRecommendation selectRetrievedProducts(final String customerRequest, final List<RetrievedProduct> products) {
        final String catalogueContext = products.stream()
                .map(RetrievedProduct::content)
                .collect(Collectors.joining("\n\n"));

        return chatClient.prompt()
                .system(promptTemplates.productSelectionSystemPrompt())
                .user(user -> user
                        .text(promptTemplates.productSelectionUserPrompt())
                        .param("customerRequest", customerRequest)
                        .param("catalogueContext", catalogueContext))
                .call().entity(ProductRecommendation.class);
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

    public ProductRecommendation recommendProductsWithTools(final String customerRequest) {
        return chatClient.prompt()
                .system(promptTemplates.toolProductSelectionSystemPrompt())
                .user(customerRequest)
                .tools(localStoreTools)
                .call()
                .entity(ProductRecommendation.class);
    }
}
