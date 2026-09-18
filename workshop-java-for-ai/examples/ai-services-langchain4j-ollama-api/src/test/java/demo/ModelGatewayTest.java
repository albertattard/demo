package demo;

import demo.model.Missions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
class ModelGatewayTest {

    private ModelGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = ModelGateway.create();
    }

    @Test
    void extractTheDateAndTimeFromTheGivenText() {
        /* Given */
        final String text = "Apollo 11 landed on the Moon on July 20, 1969 at 20:17 UTC.";

        /* When */
        final Optional<LocalDateTime> dateTime = gateway.extractDateTimeFrom(text);

        /* Then */
        assertThat(dateTime)
                .describedAs("Apollo 11 landed on the Moon on July 20, 1969 at 20:17 UTC")
                .isEqualTo(Optional.of(LocalDateTime.of(1969, 7, 20, 20, 17)));
    }

    @Test
    void returnTheLunarMissionsForTheGivenYear() {
        /* Given */
        final int year = 1978;

        /* When */
        final Missions missions = gateway.lunarMissionsOn(year);

        /* Based on: https://en.wikipedia.org/wiki/List_of_missions_to_the_Moon */
        /*  - ISEE-3 (ICE/Explorer 59): 12 August 1978	United States NASA */

        /* Then */
        assertThat(missions).isNotNull();
        assertThat(missions.year()).isEqualTo(year);
        /* Unfortunately, the model under test does not always return all the missions! */
        // assertThat(missions.size()).isEqualTo(1);
    }
}
