package demo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
@SpringBootTestWithTestProfile
class ModelGatewayIntegrationTest {

    @Autowired
    private ModelGateway gateway;

    @Test
    void returnTheDocumentsBasedOnTheirRelevance() {
        /* Given */
        final Map<String, String> data = Map.of(
                "humans-first-lunar-landing", """
                        Humans first landed on the Moon on July 20, 1969, during NASA’s Apollo 11 mission.
                        Astronauts Neil Armstrong and Buzz Aldrin became the first humans to set foot on the lunar \
                        surface, while Michael Collins remained in lunar orbit aboard the command module.
                        
                        Armstrong’s famous first step occurred at 02:56 UTC on July 21, 1969 (10:56 PM EDT, July \
                        20, 1969).""",
                "monalisa-painting", """
                        The Mona Lisa, painted by Leonardo da Vinci in the early 16th century, is one of the most \
                        famous artworks in the world. Renowned for its mysterious expression, detailed realism, \
                        and innovative use of sfumato, it depicts a seated woman whose enigmatic smile has \
                        fascinated viewers for centuries.""",
                "pyramids-of-egypt", """
                        The Pyramids of Egypt, especially the Great Pyramid of Giza, are monumental tombs built \
                        over 4,000 years ago as resting places for pharaohs. They remain one of the most iconic \
                        symbols of ancient engineering, reflecting both the Egyptians’ architectural skill and \
                        their deep religious beliefs about the afterlife.""");
        data.forEach((slug, text) -> gateway.add(text, Map.of("slug", slug)));

        /* When */
        final List<ModelGateway.Result> results = gateway.search("When did humans first land on the Moon?");

        /* Then */
        assertThat(results.size())
                .describedAs("All three entries should be returned")
                .isEqualTo(3);
        assertThat(results.getFirst().slug())
                .describedAs("The entry describing the lunar landing should have the best score given the moon landing question")
                .isEqualTo("humans-first-lunar-landing");
    }
}
