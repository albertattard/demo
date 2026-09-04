package demo;

import java.util.Arrays;

public final class Main {

    public static void main(final String[] args) {
        final String prompt = readPromptFromArgs(args);
        System.out.println("prompt> " + prompt);

        final ModelGateway gateway = ModelGateway.create();
        final String assistant = gateway.prompt(prompt);
        System.out.println("assistant> " + assistant.translateEscapes());
    }

    private static String readPromptFromArgs(final String[] args) {
        return Arrays.stream(args)
                .findFirst()
                .orElse("""
                        Please suggest appropriate character names for the following professions in a fictional story
                        - Doctor
                        - Nurse
                        - Engineer
                        - Taxi Driver
                        - Teacher
                        - Receptionist""");
    }

    private Main() {}
}
