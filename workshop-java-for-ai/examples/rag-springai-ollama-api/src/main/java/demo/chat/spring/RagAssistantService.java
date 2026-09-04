package demo.chat.spring;

import demo.chat.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static demo.chat.spring.DocumentUtils.*;

@Service
public class RagAssistantService implements AssistantService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RagAssistantService.class);

    private final ChatClient chatClient;
    private final Advisor advisor;
    private final String systemPrompt;

    public RagAssistantService(final ChatClient.Builder builder, final Advisor advisor, @Qualifier("systemPrompt") String systemPrompt) {
        this.chatClient = builder.build();
        this.advisor = advisor;
        this.systemPrompt = systemPrompt;
    }

    @Override
    public AssistantResponse reply(final List<DialogueMessage> history, final String userText) {
        LOGGER.debug("Calling the AI");
        LOGGER.debug("Query: {}", userText);
        LOGGER.debug("History: {}", history);

        final ChatClientResponse response = chatClient.prompt()
                .system(systemPrompt)
                .advisors(advisor, SimpleLoggerAdvisor.builder().order(1).build())
                .messages(history.stream().map(RagAssistantService::toMessage).toList())
                .user(userText)
                .call()
                .chatClientResponse();

        LOGGER.debug("Processing the response");

        final Map<String, AssistantReference> referencesByPath = new HashMap<>();
        /* TODO: There is a dependency in the type of Advisor used. Try to find a more generic way of dealing with this */
        final String[] keys = {RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT, QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS};
        for (final String key : keys) {
            if (response.context().get(key) instanceof final List<?> list) {
                for (final Object object : list) {
                    if (object instanceof final Document document) {
                        final String title = title(document).orElse(null);
                        final String subtitle = subtitle(document).orElse(null);
                        final String slug = slug(document).orElse(null);
                        final String path = path(document).orElse(null);

                        if (title != null && slug != null && path != null) {
                            final double score = score(document).orElse(0.0);
                            referencesByPath.merge(path, new AssistantReference(title, subtitle, slug, path, score), AssistantReference::max);
                        }
                    }
                }
            }
        }

        final String text = Optional.ofNullable(response.chatResponse())
                .map(ChatResponse::getResult)
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .orElse("No text response");

        final ParsedResponse parsed = ParsedResponse.parse(text);
        final LinkedHashSet<AssistantReference> references = parsed.references().stream()
                .map(referencesByPath::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LOGGER.debug("--------------------------------------------------------------------------------");
        LOGGER.debug("Text: {}", text);
        LOGGER.debug("Parsed: {}", parsed);
        LOGGER.debug("References: {}", references);
        LOGGER.debug("--------------------------------------------------------------------------------");

        return new AssistantResponse(parsed.text(), references);
    }

    private static Message toMessage(final DialogueMessage dialogueMessage) {
        return switch (dialogueMessage) {
            case DialogueMessage.User user -> new UserMessage(user.text());
            case DialogueMessage.Assistant assistant -> new AssistantMessage(assistant.text());
        };
    }
}
