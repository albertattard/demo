package demo;

import com.oracle.bmc.ClientConfiguration;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
import com.oracle.bmc.generativeaiinference.model.*;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.bmc.retrier.RetryConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

final class ModelGateway implements AutoCloseable {

    private final GenerativeAiInferenceClient client;
    private final String modelOcid;
    private final String compartmentOcid;

    static ModelGateway create() {
        return new ModelGateway(ModelGatewayProperties.load());
    }

    String prompt(final String prompt) {
        requireNonNull(prompt);

        final Message message = UserMessage.builder()
                .content(List.of(TextContent.builder().text(prompt).build()))
                .build();

        final GenericChatRequest chatRequest = GenericChatRequest.builder()
                .messages(List.of(message))
                .isStream(false)
                .build();

        final ChatDetails details = ChatDetails.builder()
                .servingMode(OnDemandServingMode.builder().modelId(modelOcid).build())
                .compartmentId(compartmentOcid)
                .chatRequest(chatRequest)
                .build();

        final ChatRequest request = ChatRequest.builder()
                .chatDetails(details)
                .build();

        final ChatResult result = client.chat(request).getChatResult();
        return result.getChatResponse() instanceof final GenericChatResponse response
                ? extractResponse(response)
                : "";
    }

    private static String extractResponse(final GenericChatResponse chatResponse) {
        return chatResponse.getChoices().stream()
                .flatMap(choice -> choice.getMessage().getContent().stream()
                        .filter(content -> content instanceof TextContent)
                        .map(content -> (TextContent) content)
                        .map(TextContent::getText))
                .collect(Collectors.joining("\n"));
    }

    @Override
    public void close() {
        client.close();
    }

    private ModelGateway(final ModelGatewayProperties properties) {
        this.modelOcid = properties.modelOcid();
        this.compartmentOcid = properties.compartmentOcid();
        this.client = GenerativeAiInferenceClient.builder()
                .region(properties.region())
                .configuration(createClientConfiguration())
                .build(createAuthenticationDetailsProvider(properties));
    }

    private static ClientConfiguration createClientConfiguration() {
        return ClientConfiguration.builder()
                .readTimeoutMillis(240000)
                .retryConfiguration(RetryConfiguration.NO_RETRY_CONFIGURATION)
                .build();
    }

    private static AbstractAuthenticationDetailsProvider createAuthenticationDetailsProvider(final ModelGatewayProperties properties) {
        try {
            return switch (properties.configuration()) {
                case ModelGatewayProperties.FileBasedConfiguration file ->
                        new ConfigFileAuthenticationDetailsProvider(ConfigFileReader.parse(file.file(), file.profile()));
            };
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to create the authentication provider", e);
        }
    }
}
