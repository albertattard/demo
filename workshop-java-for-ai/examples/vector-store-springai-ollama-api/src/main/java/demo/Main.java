package demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class Main {

    public static void main(final String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    @Profile("!test")
    CommandLineRunner createRunner(final ModelGateway gateway) {
        return args -> {
            final Map<String, String> data = Map.of(
                    "humans-first-lunar-landing", """
                            Humans first landed on the Moon on July 20, 1969, during NASA’s Apollo 11 mission.
                            Astronauts Neil Armstrong and Buzz Aldrin became the first humans to set foot on the lunar \
                            surface, while Michael Collins remained in lunar orbit aboard the command module.
                            
                            Armstrong’s famous first step occurred at 02:56 UTC on July 21, 1969 (10:56 PM EDT, July \
                            20, 1969).""",
                    "monalisa-painting", """
                            The Mona Lisa, painted by Leonardo da Vinci in the early 16th century, is one of the most \
                            famous artworks in the world. Renowned for its mysterious expression, detailed realism, \
                            and innovative use of sfumato, it depicts a seated woman whose enigmatic smile has \
                            fascinated viewers for centuries.""",
                    "pyramids-of-egypt", """
                            The Pyramids of Egypt, especially the Great Pyramid of Giza, are monumental tombs built \
                            over 4,000 years ago as resting places for pharaohs. They remain one of the most iconic \
                            symbols of ancient engineering, reflecting both the Egyptians’ architectural skill and \
                            their deep religious beliefs about the afterlife.""");

            data.forEach((slug, text) -> gateway.add(text, Map.of("slug", slug)));


            System.out.println();
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Find by criteria");
            data.keySet().stream()
                    .limit(2)
                    .forEach(slug -> {
                        System.out.println("slug> " + slug);
                        gateway.findByMetadataSlug(slug)
                                .forEach(result -> System.out.printf("result> [%s] %s...%n", result.id(), result.text().substring(0, 30)));
                    });


            System.out.println();
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Find by similarity");
            final List<String> queries = List.of("What purpose did the pyramids serve in ancient Egyptian society?",
                    "When did humans first land on the Moon?",
                    "Why is the Mona Lisa considered so important?");
            queries.forEach(query -> {
                System.out.println("query> " + query);
                gateway.search(query)
                        .forEach(result -> System.out.printf("result> [%s] %s... (Score: %.04f)%n", result.id(), result.text().substring(0, 30), result.score()));
            });

            System.out.println();
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Find by similarity (using German words)");
            final List<String> queriesInGerman = List.of("Welchem Zweck dienten die Pyramiden in der altägyptischen Gesellschaft?",
                    "Wann landeten Menschen erstmals auf dem Mond?",
                    "Warum gilt die Mona Lisa als so bedeutend?");
            queriesInGerman.forEach(query -> {
                System.out.println("query> " + query);
                gateway.search(query)
                        .forEach(result -> System.out.printf("result> [%s] %s... (Score: %.04f)%n", result.id(), result.text().substring(0, 30), result.score()));
            });
        };
    }
}
