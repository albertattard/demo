package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
class ModelGateway {

    private final ChatClient client;

    private ModelGateway(final ChatClient.Builder builder) {
        this.client = builder.build();
    }

    /* Both parameters are plain strings and are vulnerable to injection attacks. */
    String translate(final String text, final Language language) {
        final PromptTemplate template = new PromptTemplate("""
                Translate the following to {language}. Return only the translated version.
                
                {text}""");

        final Prompt prompt = template.create(Map.of(
                "language", language,
                "text", text));

        return client.prompt(prompt)
                .call()
                .content();
    }
}
