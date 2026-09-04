package demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import static demo.MathUtils.noise;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ModelTest {

    @Nested
    class ClassifyTest {

        private Model model;

        @BeforeEach
        void setUp() {
            model = Model.createWithoutHiddenLayers(7, 10);

            /* 7-segment patterns for digits 0–9 (A-G: 1=on, 0=off) */
            final double[][] inputs = {
                    {1, 1, 1, 1, 1, 1, 0}, // 0
                    {0, 1, 1, 0, 0, 0, 0}, // 1
                    {1, 1, 0, 1, 1, 0, 1}, // 2
                    {1, 1, 1, 1, 0, 0, 1}, // 3
                    {0, 1, 1, 0, 0, 1, 1}, // 4
                    {1, 0, 1, 1, 0, 1, 1}, // 5
                    {1, 0, 1, 1, 1, 1, 1}, // 6
                    {1, 1, 1, 0, 0, 0, 0}, // 7
                    {1, 1, 1, 1, 1, 1, 1}, // 8
                    {1, 1, 1, 1, 0, 1, 1}  // 9
            };

            /* Expected outputs */
            final double[][] targets = new double[10][10];
            for (int i = 0; i < 10; i++) {
                targets[i][i] = 1;
            }

        /* Training
           Learning rate controls how much weights are adjusted during training.
            A smaller rate means slower but more stable learning. */
            final double learningRate = 0.1;
            for (int epoch = 0; epoch < 1_000; epoch++) {
                for (int i = 0; i < inputs.length; i++) {
                    for (int j = 0; j < 10; j++) {
                        model.train(noise(inputs[i]), targets[i], learningRate);
                    }
                }
            }
        }

        @ParameterizedTest
        @MethodSource("allOptions")
        void returnCorrectPredictionEvenWhenGivenNoisyInputs(final double[] input, final int expected) {
            /* Given */
            final double[] inputWithNoise = noise(input);

            /* When */
            final int prediction = model.classify(inputWithNoise);

            /* Then */
            assertThat(prediction)
                    .describedAs("The inputs " + Arrays.toString(input) + "(with noise " + Arrays.toString(inputWithNoise) + ") should be predicted as " + expected)
                    .isEqualTo(expected);
        }

        static Stream<Arguments> allOptions() {
            return Stream.of(
                    Arguments.of(new double[]{1, 1, 1, 1, 1, 1, 0}, 0),
                    Arguments.of(new double[]{0, 1, 1, 0, 0, 0, 0}, 1),
                    Arguments.of(new double[]{1, 1, 0, 1, 1, 0, 1}, 2),
                    Arguments.of(new double[]{1, 1, 1, 1, 0, 0, 1}, 3),
                    Arguments.of(new double[]{0, 1, 1, 0, 0, 1, 1}, 4),
                    Arguments.of(new double[]{1, 0, 1, 1, 0, 1, 1}, 5),
                    Arguments.of(new double[]{1, 0, 1, 1, 1, 1, 1}, 6),
                    Arguments.of(new double[]{1, 1, 1, 0, 0, 0, 0}, 7),
                    Arguments.of(new double[]{1, 1, 1, 1, 1, 1, 1}, 8),
                    Arguments.of(new double[]{1, 1, 1, 1, 0, 1, 1}, 9)
            );
        }
    }

    @Nested
    class SerialisationTest {

        @Test
        void serialise() throws IOException {
            final Model model = Model.createWithoutHiddenLayers(7, 10);

            final Path file = Files.createTempFile("model", ".data");
            model.writeTo(file);
            final Model read = Model.readFrom(file);

            assertThat(read)
                    .isEqualTo(model);
        }
    }
}
