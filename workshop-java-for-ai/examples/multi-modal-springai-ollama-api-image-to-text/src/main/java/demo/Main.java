package demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;
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
            final Path image = readImagePathFromArgs(args);
            System.out.println("image> " + image);

            final String description = gateway.describeImage(image);
            System.out.println("description> " + description);
        };
    }

    private static Path readImagePathFromArgs(final String[] args) {
        return Arrays.stream(args)
                .map(Path::of)
                .findFirst()
                .orElse(Path.of("assets", "images", "Image-1.jpg"));
    }
}
