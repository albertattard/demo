package demo;

import demo.model.Language;
import demo.model.Missions;
import demo.services.DateTimeService;
import demo.services.LunarMissionsService;
import demo.services.TextService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

final class ModelGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelGateway.class);

    private final TextService textService;
    private final DateTimeService dateTimeService;
    private final LunarMissionsService lunarMissionsService;

    static ModelGateway create() {
        return new ModelGateway();
    }

    String summarise(final String text) {
        requireNonNull(text);

        return textService.summarise(text);
    }

    String translateTo(final String text, final Language language) {
        requireNonNull(text);
        requireNonNull(language);

        return textService.translateTo(text, language);
    }

    Missions lunarMissionsOn(final int year) {
        if (year < 1900 || year > 2077) {
            throw new IllegalArgumentException("Invalid year " + year + ". The year must be between 1900 and 2077");
        }

        return lunarMissionsService.missionsOn(year);
    }

    Optional<LocalDateTime> extractDateTimeFrom(final String text) {
        requireNonNull(text);

        try {
            final LocalDateTime extracted = dateTimeService.extractDateTimeFrom(text);
            return Optional.ofNullable(extracted);
        } catch (final RuntimeException e) {
            /* Failed to parse the response from the model */
            LOGGER.warn("Failed to parse the response from the model", e);
            return Optional.empty();
        }
    }

    private ModelGateway() {
        final ChatModel chatModel = OllamaChatModel.builder()
                .modelName("llama3.1")
                .baseUrl("http://localhost:11434/")
                .timeout(Duration.ofMinutes(2))
                .temperature(0.0)
                .build();

        textService = AiServices.builder(TextService.class)
                .chatModel(chatModel)
                .build();

        dateTimeService = AiServices.builder(DateTimeService.class)
                .chatModel(chatModel)
                .build();

        lunarMissionsService = AiServices.builder(LunarMissionsService.class)
                .chatModel(chatModel)
                .build();
    }
}
