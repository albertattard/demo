package demo;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Tag("e2e")
@SpringBootTestWithTestProfile(webEnvironment = RANDOM_PORT)
class RecipeMcpServerTest {

    private McpSyncClient client;

    @BeforeEach
    void beforeEach(@LocalServerPort final int localPort) {
        final McpClientTransport transport = HttpClientStreamableHttpTransport.builder("http://localhost:" + localPort).build();

        client = McpClient.sync(transport).build();
        client.initialize();
    }

    @Nested
    class ToolsTest {
        @Nested
        class RecipeCountTest {
            @Test
            void returnTheNumberOfRecipes() {
                final McpSchema.CallToolResult result = client.callTool(new McpSchema.CallToolRequest("recipeCount", Map.of()));

                final Optional<String> value = result.content().stream()
                        .filter(c -> c instanceof McpSchema.TextContent)
                        .map(c -> (McpSchema.TextContent) c)
                        .map(McpSchema.TextContent::text)
                        .findFirst();

                assertThat(value)
                        .isEqualTo(Optional.of("6"));
            }
        }

        @Nested
        class FindRecipesByTypeTest {
            @Test
            void returnTheNumberOfTypeMain() {
                final McpSchema.CallToolResult result = client.callTool(new McpSchema.CallToolRequest("findRecipesByType", Map.of("type", "MAIN")));

                final List<String> recipes = result.content().stream()
                        .filter(c -> c instanceof McpSchema.TextContent)
                        .map(c -> (McpSchema.TextContent) c)
                        .map(McpSchema.TextContent::text)
                        .flatMap(readList())
                        .map(Recipe::title)
                        .sorted()
                        .toList();

                assertThat(recipes)
                        .isEqualTo(List.of("Classic Garlic & Rosemary Roast Lamb", "Creamy Chickpea & Spinach Coconut Curry"));
            }

            private static Function<String, Stream<? extends Recipe>> readList() {
                return json -> {
                    try {
                        return McpJsonDefaults.getMapper()
                                .readValue(json, new TypeRef<List<Recipe>>() {}).stream();
                    } catch (final IOException e) {
                        throw new UncheckedIOException("Failed to parse the result", e);
                    }
                };
            }
        }
    }

    @Nested
    class PromptsTest {
        @Nested
        class RecipeAssistantPromptTest {
            @Test
            void recipeAssistantPromptIsExposed() {
                /* Given */
                /* When */
                final McpSchema.ListPromptsResult prompts = client.listPrompts();

                /* Then */
                final boolean found = prompts.prompts().stream()
                        .anyMatch(p -> "recipeAssistant".equals(p.name()));
                assertThat(found)
                        .as("Expected recipeAssistant to be listed by the MCP server")
                        .isTrue();
            }

            @Test
            void buildMessagesFromUserRequest() {
                /* Given */
                final String userRequest = "How many recipes do we have?";
                final McpSchema.GetPromptRequest request = new McpSchema.GetPromptRequest(
                        "recipeAssistant",
                        Map.of("userRequest", userRequest));

                /* When */
                final McpSchema.GetPromptResult result = client.getPrompt(request);

                /* Then */
                assertThat(result.description())
                        .isEqualTo("A prompt that assists users with recipe queries using recipeCount and findRecipesByType.");
                assertThat(result.messages())
                        .hasSize(2);

                final McpSchema.PromptMessage assistantMessage = result.messages().getFirst();
                assertThat(assistantMessage.role())
                        .isEqualTo(McpSchema.Role.ASSISTANT);

                final String assistantText = ((McpSchema.TextContent) assistantMessage.content()).text();
                assertThat(assistantText)
                        .contains("recipeCount")
                        .contains("findRecipesByType");

                // user message should mirror the argument
                final McpSchema.PromptMessage userMessage = result.messages().get(1);
                assertThat(userMessage.role())
                        .isEqualTo(McpSchema.Role.USER);

                final String userText = ((McpSchema.TextContent) userMessage.content()).text();
                assertThat(userText)
                        .isEqualTo(userRequest);
            }
        }
    }

    @Nested
    class ResourcesTest {
        @Nested
        class ListsRecipeResourcesTest {
            @Test
            void listsRecipes() {
                /* Given */
                /* When */
                final McpSchema.ListResourcesResult result = client.listResources();

                /* Then */
                assertThat(result.resources())
                        .isNotEmpty();

                // Adjust these predicates to match what you actually expose
                final boolean hasRecipesResource = result.resources().stream()
                        .anyMatch(r -> "recipes".equals(r.name()) || (r.uri() != null && r.uri().contains("recipes")));
                assertThat(hasRecipesResource)
                        .as("Expected at least one 'recipes' resource to be listed")
                        .isTrue();
            }
        }
    }

    @AfterEach
    void afterEach() {
        client.close();
    }
}
