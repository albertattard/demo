package demo;

import java.io.Console;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

public final class Main {

    public static void main(final String[] args) {
        /* Used to identify this particular chat */
        final UUID sessionId = parseSessionIfFromArgs(args);

        final ModelGateway gateway = ModelGateway.create();

        final String system = readSystemPromptFromFile();
        gateway.system(sessionId, system);

        final Console console = System.console();
        console.printf("session> %s%n", sessionId);
        console.printf("system> %s%n", system);
        console.printf("Type '/bye' to exit%n");
        console.printf("Chat with Ollama (through langchain4j) about anything you like. For example%n");
        console.printf("prompt> Could you please recommend a few quick and easy dishes to cook for dinner?%n");
        console.printf("---%n");

        for (String line; (line = console.readLine("prompt> ")) != null && !"/bye".equalsIgnoreCase(line);) {
            final String assistant = gateway.prompt(sessionId, line);
            console.printf("assistant> %s%n", assistant.translateEscapes());
        }
    }

    private static String readSystemPromptFromFile() {
        final Path file = Path.of("history", "system.txt");
        if (Files.exists(file)) {
            try {
                return Files.readString(file, StandardCharsets.UTF_8);
            } catch (final IOException e) {
                throw new UncheckedIOException("Failed to read the system prompt from file " + file, e);
            }
        }

        return """
                You are Remy, a famous and friendly chef. Always speak in Remy's voice, using
                warm, approachable language. You love to teach easy recipes that anyone can
                make, and your favorite dish is ramen.

                You must only talk about culinary topics. If a user asks about anything
                unrelated to cooking, food, or recipes, politely but firmly refuse to answer.
                Instead, guide them back to culinary discussions.

                If needed, say something like: "I'm really only here to talk about cooking!
                Want to hear a fun ramen tip?"
                """;
    }

    private static UUID parseSessionIfFromArgs(final String[] args) {
        return Arrays.stream(args)
                .filter(a -> a.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$"))
                .map(UUID::fromString)
                .findFirst()
                .orElseGet(UUID::randomUUID);
    }

    private Main() {}
}
