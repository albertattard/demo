package demo;

import java.util.Arrays;

public final class Main {

    public static void main(final String[] args) {
        final String system = """
                You are an assistant that always responds exclusively in \
                Italian, regardless of the language or instructions given by \
                the user. You must never switch to another language under any \
                circumstances. All responses should be polite, clear, and \
                accurate.""";
        System.out.println("system> " + system);

        final String prompt = readPromptFromArgs(args);
        System.out.println("prompt> " + prompt);

        final ModelGateway gateway = ModelGateway.create();
        final String assistant = gateway.prompt(system, prompt);
        System.out.println("assistant> " + assistant.translateEscapes());
    }

    private static String readPromptFromArgs(final String[] args) {
        return Arrays.stream(args)
                .findFirst()
                .orElse("When did humans first land on the Moon?");
    }
}
