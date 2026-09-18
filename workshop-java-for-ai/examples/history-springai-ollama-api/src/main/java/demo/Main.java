package demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.Console;
import java.util.Arrays;
import java.util.UUID;

@SpringBootApplication
public class Main {

    public static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    @NotTestProfile
    CommandLineRunner createRunner(final ModelGateway gateway) {
        return args -> {
            /* Used to identify this particular chat */
            final UUID sessionId = parseSessionIfFromArgs(args);

            final Console console = System.console();
            console.printf("session> %s%n", sessionId);
            console.printf("Type '/bye' to exit%n");
            console.printf("Chat with Ollama (through Spring AI) about anything you like. For example%n");
            console.printf("prompt> When did humans first land on the Moon?%n");
            console.printf("---%n");

            for (String line; (line = console.readLine("prompt> ")) != null && !"/bye".equalsIgnoreCase(line); ) {
                final String assistant = gateway.prompt(sessionId, line);
                console.printf("assistant> %s%n", assistant.translateEscapes());
            }
        };
    }

    private static UUID parseSessionIfFromArgs(final String[] args) {
        return Arrays.stream(args)
                .filter(a -> a.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$"))
                .map(UUID::fromString)
                .findFirst()
                .orElseGet(UUID::randomUUID);
    }
}
