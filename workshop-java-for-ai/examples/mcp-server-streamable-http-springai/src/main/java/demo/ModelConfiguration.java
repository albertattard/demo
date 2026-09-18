package demo;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import java.util.List;

@Configuration
class ModelConfiguration {

    @Bean
    RestClientCustomizer logbookCustomizer(final LogbookClientHttpRequestInterceptor interceptor) {
        return restClient -> restClient.requestInterceptor(interceptor);
    }

    @Bean
    ToolCallbackProvider toolCallbackProvider(final RecipeTools tools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(tools)
                .build();
    }

    @Bean
    List<McpServerFeatures.SyncPromptSpecification> recipeAssistantPrompts() {
        final McpSchema.Prompt recipeAssistantPrompt = new McpSchema.Prompt(
                "recipeAssistant",
                "A prompt to help users explore recipes using the recipe tools.",
                List.of(new McpSchema.PromptArgument(
                        "userRequest",
                        "The user's question or request about recipes",
                        true)));

        final McpServerFeatures.SyncPromptSpecification recipeAssistantPromptSpec
                = new McpServerFeatures.SyncPromptSpecification(recipeAssistantPrompt, (exchange, getPromptRequest) -> {
                    final String userRequest
                            = (String) getPromptRequest.arguments().get("userRequest");

                    final McpSchema.PromptMessage assistantMessage = new McpSchema.PromptMessage(
                            McpSchema.Role.ASSISTANT, /* This is not user input */
                            new McpSchema.TextContent("""
                                    You are a helpful recipe assistant.

                                    You have access to the following tools:

                                    - recipeCount: returns the total number of recipes in the repository.
                                    - findRecipesByType: returns recipes for a given RecipeType (DESSERT, MAIN, STARTER, ...).

                                    Rules:
                                    - If the user asks how many recipes there are, you MUST call the 'recipeCount' tool.
                                    - If the user asks for recipes for a certain course or type (e.g. dessert, main course, starter/appetizer),
                                      you MUST call 'findRecipesByType' with the matching RecipeType value.
                                    - Map natural language to enum values:
                                      * "dessert", "sweet" → DESSERT
                                      * "main", "main course" → MAIN
                                      * "starter", "appetizer" → STARTER
                                    - Do NOT invent recipes that are not returned by tools.
                                    - When tools return data, summarize and format it nicely for the user.
                                    """));

                    final McpSchema.PromptMessage userMessage = new McpSchema.PromptMessage(
                            McpSchema.Role.USER,
                            new McpSchema.TextContent(userRequest));

                    return new McpSchema.GetPromptResult(
                            "A prompt that assists users with recipe queries using recipeCount and findRecipesByType.",
                            List.of(assistantMessage, userMessage));
                });

        return List.of(recipeAssistantPromptSpec);
    }

    /* TODO: This is not working as expected and the MCP Inspector shows: MCP error -32602 */
    @Bean
    McpServerFeatures.SyncCompletionSpecification recipeAssistantCompletion() {
        // This ties the completion to your existing prompt "recipeAssistant"
        final McpSchema.PromptReference ref = new McpSchema.PromptReference("recipeAssistant");

        return new McpServerFeatures.SyncCompletionSpecification(ref, (_, request) -> {
            // Which argument is being completed? (should be "userRequest")
            final McpSchema.CompleteRequest.CompleteArgument arg = request.argument();
            final String name = arg.name();
            final String value = arg.value() != null ? arg.value() : "";

            // Only handle our known argument
            if (!"userRequest".equals(name)) {
                // No suggestions for unknown arguments
                final McpSchema.CompleteResult.CompleteCompletion empty
                        = new McpSchema.CompleteResult.CompleteCompletion(List.of(), 0, false);
                return new McpSchema.CompleteResult(empty);
            }

            // Very simple suggestion list
            final List<String> candidates = List.of(
                    "How many recipes do we have?",
                    "Show me dessert recipes",
                    "Show me main course recipes",
                    "Show me starter recipes");

            final List<String> suggestions = candidates.stream()
                    .filter(s -> s.toLowerCase().startsWith(value.toLowerCase()))
                    .limit(5)
                    .toList();

            final McpSchema.CompleteResult.CompleteCompletion completion = new McpSchema.CompleteResult.CompleteCompletion(
                    suggestions,
                    suggestions.size(),
                    false);

            return new McpSchema.CompleteResult(completion);
        });
    }

    @Bean
    List<McpServerFeatures.SyncResourceSpecification> recipeResources(final RecipeRepository repository) {
        final List<McpSchema.Role> audience = List.of(McpSchema.Role.USER);
        final McpSchema.Annotations annotations = new McpSchema.Annotations(audience, 1.0);

        final McpSchema.Resource listResource = McpSchema.Resource.builder()
                .uri("recipes://recipe-list")
                .name("Recipe List")
                .description("A list of recipes available in the repository")
                .mimeType("text/plain")
                .annotations(annotations).build();

        final McpServerFeatures.SyncResourceSpecification listResourceSpec
                = new McpServerFeatures.SyncResourceSpecification(listResource, (_, request) -> {
                    final StringBuilder listText = new StringBuilder();
                    for (final String title : repository.findAllTitles()) {
                        listText.append("- ").append(title).append("\n");
                    }

                    return new McpSchema.ReadResourceResult(
                            List.of(new McpSchema.TextResourceContents(
                                    request.uri(),
                                    "text/plain",
                                    listText.toString())));
                });

        return List.of(listResourceSpec);
    }
}
