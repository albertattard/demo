package demo;

import java.lang.management.ManagementFactory;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner javaVersionRunner() {
        return _ -> LOGGER.info("Java runtime version: {}", System.getProperty("java.runtime.version"));
    }

    @Bean
    CommandLineRunner processRunner() {
        return _ -> LOGGER.info("Process ID: {}", ProcessHandle.current().pid());
    }

    @Bean
    CommandLineRunner configurationRunner() {
        return _ -> {
            final List<String> arguments = ManagementFactory.getRuntimeMXBean()
                    .getInputArguments();

            if (arguments.isEmpty()) {
                LOGGER.info("Using default configuration");
            } else {
                LOGGER.info("Using the following JVM arguments:");
                arguments.forEach(LOGGER::info);
            }
        };
    }
}
