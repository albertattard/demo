package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
class TimeTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeTools.class);

    @Tool(name = "currentTime", description = "Get the current time in the specified time zone.")
    String currentTime(final @ToolParam(description = "The time zone") String timeZone) {
        LOGGER.debug("Getting the current time in {}", timeZone);
        return LocalDateTime.now(ZoneId.of(timeZone))
                .format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
