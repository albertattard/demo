package demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
class ModelGatewayIntegrationTest {

    private ModelGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = ModelGateway.create();
    }

    @Test
    void describesImageContainingHippopotamus() {
        /* Given */
        final Path path = Path.of("./src/test/resources/images/Image-1.jpg");

        /* When */
        final String assistant = gateway.describeImage(path);

        /* Then */
        assertThat(assistant)
                .describedAs("The temperature is set to 0, which means the description should be always the same")
                // .containsPattern("\\bhippo(potamus)?\\b")
                .isEqualTo("The image shows a hippopotamus standing on a concrete floor, drinking water from a puddle.");
    }
}
