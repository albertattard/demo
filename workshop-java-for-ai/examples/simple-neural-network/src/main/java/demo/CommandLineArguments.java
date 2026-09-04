package demo;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.IntPredicate;

import static java.util.Objects.requireNonNull;

public record CommandLineArguments(
        boolean printHelp,
        boolean trainModel,
        boolean testModel,
        Set<Integer> skipTraining) {

    public CommandLineArguments {
        skipTraining = Set.copyOf(skipTraining);
    }

    public static CommandLineArguments parse(final String[] args) {
        requireNonNull(args);

        boolean printHelp = false;
        boolean trainModel = false;
        boolean testModel = false;

        final Set<Integer> skipTraining = new HashSet<>();

        for (final String arg : args) {
            switch (arg) {
                case "--help" -> printHelp = true;
                case "--train" -> trainModel = true;
                case "--test" -> testModel = true;
                default -> {
                    if (arg.matches("^--skip=\\d+(,\\s*\\d+)*$")) {
                        skipTraining.addAll(Arrays.stream(arg.substring(7).split(",")).map(Integer::valueOf).toList());
                    } else {
                        /* Unknown option */
                        printHelp = true;
                    }
                }
            }
        }

        /* Print the help if no options are provided */
        printHelp = printHelp || (!trainModel && !testModel);

        return new CommandLineArguments(printHelp, trainModel, testModel, skipTraining);
    }

    public IntPredicate skipInputPredicate() {
        return this::skipTrainingFor;
    }

    public boolean skipTrainingFor(final int input) {
        return skipTraining.contains(input);
    }

    public static void printHelp(final PrintStream out) {
        out.println("Simple Neural Network");
        out.println("Usage");
        out.println("--help                 prints this message");
        out.println("--train                trains the model and save the parameters to file");
        out.println("--skip=<num[,num,...]> skips the provided comma separated numbers during training");
        out.println("--test                 test the trained model against unseen data using the saved parameters");
    }
}
