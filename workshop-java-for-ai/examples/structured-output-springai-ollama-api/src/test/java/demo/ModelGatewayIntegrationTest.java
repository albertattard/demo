package demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
@SpringBootTestWithTestProfile
class ModelGatewayIntegrationTest {

    @Autowired
    private ModelGateway gateway;

    @Test
    @Disabled("Unfortunately, this model is not able to return an empty string when no dates are provided")
    void returnEmptyWhenNoDateIsPresent() {
        /* Given */
        final String text = "Neil Armstrong was the first person to walk on the Moon.";

        /* When */
        final Optional<LocalDateTime> extracted = gateway.extractDateTime(text);

        /* Then */
        assertThat(extracted)
                .describedAs("""
                        There is no reference to any dates in the text""")
                .isEmpty();
    }

    @Test
    void extractSpecificDateAndTime() {
        /* Given */
        final String text = "I was born at 6:30 in the morning of November 29 1978.";

        /* When */
        final Optional<LocalDateTime> extracted = gateway.extractDateTime(text);

        /* Then */
        assertThat(extracted)
                .describedAs("""
                        The model should easily extract the specific date and time from the given text""")
                .isEqualTo(Optional.of(LocalDateTime.of(1978, 11, 29, 6, 30)));
    }

    @Test
    void extractRelativeDateAndSpecificTime() {
        /* Given */
        final String text = "Can we meet tomorrow at 2pm?";

        /* When */
        final Optional<LocalDateTime> extracted = gateway.extractDateTime(text);

        /* Then */
        assertThat(extracted)
                .describedAs("""
                        The model should easily extract the relative date (tomorrow) and specific time from the given text""")
                .isEqualTo(Optional.of(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(14, 0))));
    }
}
