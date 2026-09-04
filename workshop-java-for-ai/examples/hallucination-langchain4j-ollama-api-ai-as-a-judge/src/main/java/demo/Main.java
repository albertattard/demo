package demo;

import java.util.Arrays;

public final class Main {

    public static void main(final String[] args) {
        createRunner(ModelService.create())
                .run(args);
    }

    static CommandLineRunner createRunner(final ModelService service) {
        return args -> {
            final String prompt = readPromptFromArgs(args);
            System.out.println("prompt> " + prompt);

            final ModelService.Result result = service.prompt(prompt);

            switch (result) {
                case ModelService.Result.Ok ok -> {
                    System.out.println("score> " + ok.score());
                    if (ok.score() < 7) {
                        System.out.println("judge> Unfortunately, the model was unable to provide a response to this query with the required level of reliability.");
                    }
                    System.out.println("assistant> " + ok.assistant().translateEscapes());
                }
                case ModelService.Result.Error e -> {
                    System.out.println("Failed to generate a reliable response for the given prompt");
                    System.out.println("judge> " + e.judge().translateEscapes());
                    System.out.println("assistant> " + e.assistant().translateEscapes());
                }
            }
        };
    }

    private static String readPromptFromArgs(final String[] args) {
        return Arrays.stream(args)
                .findFirst()
                .orElse("When did humans first land on the Moon?");
    }

    private Main() {}

    @FunctionalInterface
    interface CommandLineRunner {
        void run(String... args);
    }
}
