package demo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
@SpringBootTestWithTestProfile
class ModelGatewayIntegrationTest {

    @Autowired
    private ModelGateway gateway;

    @Test
    void returnTheNumberOfFilesInTheDirectory() {
        /* Given */
        final String prompt = """
                How many files are there? \
                Please return only the number of files as a number without any description or file listing.""";

        /* When */
        final String assistant = gateway.prompt(prompt);

        /* Then */
        assertThat(assistant)
                .describedAs("There are six recipes and one travel plan")
                .isEqualTo("7");
    }
}
