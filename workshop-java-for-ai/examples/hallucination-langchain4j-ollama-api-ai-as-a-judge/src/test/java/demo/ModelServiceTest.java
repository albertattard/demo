package demo;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
class ModelServiceTest {

    private ModelService service;

    @BeforeEach
    void setUp() {
        service = ModelService.create();
    }

    @Test
    void returnLowScoreAsTheModelHallucinates() {
        /* Given */
        final String prompt = "When did humans first land on the Moon?";

        /* When */
        final ModelService.Result result = service.prompt(prompt);

        /* Then */
        assertThat(result)
                .describedAs("The model hallucinates and thus the score should be less than 5")
                .isInstanceOf(ModelService.Result.Ok.class)
                .extracting(r -> ((ModelService.Result.Ok) r).score())
                .asInstanceOf(InstanceOfAssertFactories.INTEGER)
                .isLessThan(5);
    }

    @Test
    void returnHighScoreAsTheModelAnswersCorrectly() {
        /* Given */
        final String prompt = "Who painted the Mona Lisa?";

        /* When */
        final ModelService.Result result = service.prompt(prompt);

        /* Then */
        assertThat(result)
                .describedAs("The model answers correctly thus the score should be 8 or higher")
                .isInstanceOf(ModelService.Result.Ok.class)
                .extracting(r -> ((ModelService.Result.Ok) r).score())
                .asInstanceOf(InstanceOfAssertFactories.INTEGER)
                .isGreaterThanOrEqualTo(8);
    }
}
