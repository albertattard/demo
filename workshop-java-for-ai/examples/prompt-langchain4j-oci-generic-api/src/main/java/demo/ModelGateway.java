package demo;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import dev.langchain4j.community.model.oracle.oci.genai.OciGenAiChatModel;
import dev.langchain4j.model.chat.ChatModel;

import java.io.IOException;
import java.io.UncheckedIOException;

import static java.util.Objects.requireNonNull;

final class ModelGateway {

    private final ChatModel chatModel;

    static ModelGateway create() {
        return new ModelGateway(ModelGatewayProperties.load());
    }

    String prompt(final String prompt) {
        requireNonNull(prompt);

        return this.chatModel.chat(prompt);
    }

    private ModelGateway(final ModelGatewayProperties properties) {
        this.chatModel = OciGenAiChatModel.builder()
                .modelName(properties.modelName())
                .compartmentId(properties.compartmentOcid())
                .authProvider(createAuthenticationDetailsProvider(properties))
                .build();
    }

    private static BasicAuthenticationDetailsProvider createAuthenticationDetailsProvider(final ModelGatewayProperties properties) {
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
