package demo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FixtureProducer {

    public static void create(final Path directory) {
        createFixturesDirectory(directory);
        for (int i = 0; i < 24; i++) {
            writeFixture(directory.resolve("Fixture-%02d.txt".formatted(i)));
        }
    }

    private static void writeFixture(final Path path) {
        final String[] names = {"Noah", "Emilia", "Matteo", "Emma", "Elias", "Sophia", "Leon", "Hannah", "Paul", "Mia",
                "Theo", "Ella", "Luca", "Mila", "Finn", "Lina", "Liam", "Lia", "Emil", "Leni", "Louis", "Clara",
                "Henry", "Lea", "Ben", "Marie", "Felix", "Ida", "Leo", "Lilly"};

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (int i = 0; i < 2_000_000; i++) {
                writer.write(names[i % names.length]);
                writer.newLine();
            }
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to create fixture", e);
        }
    }

    private static void createFixturesDirectory(final Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to create fixtures directory", e);
        }
    }
}
