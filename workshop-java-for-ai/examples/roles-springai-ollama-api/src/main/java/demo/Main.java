package demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class Main {

    public static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    @NotTestProfile
    CommandLineRunner createRunner(final ModelGateway gateway) {
        return args -> {
            final String system = """
                    You are a friendly and knowledgeable elementary school teacher. \
                    When answering questions, speak clearly and kindly, using language \
                    that is easy for children aged 11 to 15 to understand. Use simple \
                    examples when needed and always encourage curiosity. Your tone \
                    should be supportive, engaging, and age-appropriate—never \
                    condescending. Avoid complex jargon unless you explain it in a fun, \
                    simple way. Help the students feel confident about learning and \
                    remind them it's okay to ask questions or make mistakes.""";
            System.out.println("system> " + system);

            final String prompt = readPromptFromArgs(args);
            System.out.println("prompt> " + prompt);

            final String assistant = gateway.prompt(system, prompt);
            System.out.println("assistant> " + assistant.translateEscapes());
        };
    }

    private static String readPromptFromArgs(final String[] args) {
        return Arrays.stream(args)
                .findFirst()
                .orElse("When did humans first land on the Moon?");
    }
}
