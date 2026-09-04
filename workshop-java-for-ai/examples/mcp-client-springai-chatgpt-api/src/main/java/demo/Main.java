package demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class Main {

    public static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    @NotTestProfile
    CommandLineRunner createRunner(final ModelGateway gateway, final ConfigurableApplicationContext context) {
        return args -> {
            try {
                final String prompt = readPromptFromArgs(args);
                System.out.println("prompt> " + prompt);

                final String assistant = gateway.prompt(prompt);
                System.out.println("assistant> " + assistant.translateEscapes());
            } finally {
                /* The MCP client keeps the application alive. Close the context once ready so that the application
                   exits once the command line runner completes. */
                context.close();
            }
        };
    }

    private static String readPromptFromArgs(final String[] args) {
        return Arrays.stream(args)
                .findFirst()
                .orElse("""
                        How many files are there? \
                        Please return only the number of files as a number without any description or file listing.""");
    }
}
