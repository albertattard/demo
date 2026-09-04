package demo.services;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.time.LocalDateTime;

public interface DateTimeService {
    @SystemMessage("Extract and only return the date and time from the given text in the format: ISO 8601 Format")
    @UserMessage("{{text}}.")
    LocalDateTime extractDateTimeFrom(@V("text") String text);
}
