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
                        Q: When did Albert Einstein submit his paper on special relativity to the University of Zurich?
                        A: July 30, 1905
                        
                        Q: When was the first Sputnik launched?
                        A: October 4, 1957
                        
                        Q: When was the first version of Java released?
                        A: May 23, 1995
                        
                        Q: When did humans first land on the Moon?""");
    }

    private Main() {}
}
