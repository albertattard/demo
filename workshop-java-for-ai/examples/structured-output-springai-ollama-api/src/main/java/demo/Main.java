package demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@SpringBootApplication
public class Main {

    public static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    @NotTestProfile
    CommandLineRunner createRunner(final ModelGateway gateway) {
        return args -> {
            final String text = readTextFromArgs(args);
            System.out.println("text> " + text);

            final Optional<LocalDateTime> extracted = gateway.extractDateTime(text);
            System.out.println("extracted> " + extracted.map(Objects::toString).orElse("No date found!"));
        };
    }

    private static String readTextFromArgs(final String[] args) {
        return Arrays.stream(args)
                .findFirst()
                .orElse("Can we meet at 2pm tomorrow?");
    }
}
