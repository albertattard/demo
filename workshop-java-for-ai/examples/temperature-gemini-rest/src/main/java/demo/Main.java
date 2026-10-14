package demo;

import java.util.Arrays;

public final class Main {

    public static void main(final String[] args) {
        final String model = "gemini-2.5-flash";
        final String prompt = "When did humans first land on the Moon?";
        final double temperature = readTemperatureFromArgs(args);

        System.out.println("prompt> " + prompt);
        System.out.println("temperature> " + temperature);

        /* When setting the temperature 0, this AI Model will always respond with the following:
           "Humans first landed on the Moon on **July 20, 1969**.\n\nThis historic event was accomplished by the United
           States' **Apollo 11** mission, with astronaut **Neil Armstrong** being the first person to step onto the
           lunar surface, followed shortly by **Buzz Aldrin**. Michael Collins remained in orbit aboard the command
           module." */
        try (ModelGateway gateway = ModelGateway.create()) {
            final String assistant = gateway.prompt(model, prompt, temperature);
            System.out.println("assistant> " + assistant.translateEscapes());
        }
    }

    private static double readTemperatureFromArgs(final String[] args) {
        return Arrays.stream(args)
                .filter(arg -> arg.matches("^(0(\\.\\d+)?|1(\\.\\d+)?|2(\\.0+)?)$")) /* Accepts inputs between 0.0 and 2.0 (both inclusive) */
                .limit(1)
                .mapToDouble(Double::parseDouble)
                .findFirst()
                .orElse(0);
    }

    private Main() {}
}
