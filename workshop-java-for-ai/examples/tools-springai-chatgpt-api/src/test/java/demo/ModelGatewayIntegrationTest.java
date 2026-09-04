package demo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("e2e")
@SpringBootTestWithTestProfile
class ModelGatewayIntegrationTest {

    @Autowired
    private ModelGateway gateway;

    @MockitoBean
    private TimeTools timeTools;

    @Test
    void useTheTimeToolToFetchTheCurrentTime() {
        /* Given */
        final String city = "Las Vegas";
        when(timeTools.currentTime(anyString())).thenReturn("2077-04-27T12:34:56");

        /* When */
        final String time = gateway.timeIn(city);

        /* Then */
        assertThat(time)
                .describedAs("Should use the time returned by the tool")
                .isIn(Set.of("The current time in Las Vegas is 12:34 PM on April 27, 2077",
                        "The current time in Las Vegas is 12:34 PM on April 27, 2077."));
        verify(timeTools).currentTime(anyString());
    }
}
