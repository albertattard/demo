package demo;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

public final class Main {

    public static void main(final String[] args) {
        final Path directory = Path.of("./target/fixtures");
        FixtureProducer.create(directory);

        final LocalDateTime start = LocalDateTime.now();
        final int size = new ReadNames()
                .read(directory)
                .size();
        final LocalDateTime end = LocalDateTime.now();
        System.out.println("Read " + size + " lines in " + Duration.between(start, end).toMillis() + " ms");
    }
}
