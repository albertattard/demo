package demo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.IntPredicate;

import static demo.MathUtils.noise;

public final class Main {

    // 7-segment patterns for digits 0–9 (A-G: 1=on, 0=off)
    private final static double[][] inputs = {
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

    /* In this example, the sigmoid function is used to normalise the output, resulting in values of either 0 or 1. For
        simplicity, the output layer consists of 10 neurons, with only one active (1) at a time to represent a specific
        digit. For instance, if the first neuron is active, the input corresponds to 0; if the last neuron is active,
        the input corresponds to 9. This approach can be improved by using binary representation with only four output
        neurons, or by switching to a ReLU activation function with a single output neuron. */
    private final static double[][] targets = createExpectedOutputs();

    public static void main(final String[] args) {
        final CommandLineArguments cla = CommandLineArguments.parse(args);

        if (cla.printHelp()) {
            CommandLineArguments.printHelp(System.out);
            return;
        }

        final Path modelFile = Path.of("data", "model.data");

        if (cla.trainModel()) {
            trainModel(modelFile, cla.skipInputPredicate());
        }

        if (cla.testModel()) {
            if (!Files.isRegularFile(modelFile)) {
                System.out.println("Missing model file. Use the --train to train the model first.");
                return;
            }
            testModel(modelFile);
        }
    }

    private static void trainModel(final Path path, final IntPredicate skipInputPredicate) {
        final Model model = Model.createWithoutHiddenLayers(7, 10);

        /* Training */
        System.out.println("Trained network using Supervised Learning");
        for (int epoch = 0; epoch < 1_000; epoch++) {
            for (int i = 0; i < inputs.length; i++) {
                if (skipInputPredicate.test(i)) {
                    /* Print this for the first epoch and not for all otherwise it will pollute the output */
                    if (epoch == 0) {System.out.println("Skipping training for input " + i);}
                    continue;
                }
                for (int j = 0; j < 10; j++) {
                    model.train(noise(inputs[i]), targets[i], 0.1);
                }
            }
        }

        model.writeTo(path);
    }

    private static void testModel(final Path path) {
        final Model model = Model.readFrom(path);

        /* Testing */
        System.out.println("Testing trained network");
        for (int i = 0; i < inputs.length; i++) {
            System.out.println("Input (" + i + ") predicted as: " + model.classify(noise(inputs[i])));
        }
    }

    private static double[][] createExpectedOutputs() {
        final double[][] targets = new double[10][10];
        for (int i = 0; i < 10; i++) {
            targets[i][i] = 1;
        }
        return targets;
    }

    private Main() {}
}
