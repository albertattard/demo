package demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public final class ReadNames {

    private final List<String> names = new ArrayList<>();

    public ReadNames read(final Path directory) {
        final List<Path> files = listFiles(directory);

        try (ExecutorService executor = Executors.newFixedThreadPool(files.size())) {
            final CompletionService<List<String>> completionService = new ExecutorCompletionService<>(executor);
            files.forEach(file -> completionService.submit(() -> readNamesFromFile(file)));

            for (int i = 0; i < files.size(); i++) {
                try {
                    final List<String> names = completionService.take().get();
                    this.names.addAll(names);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for result", e);
                } catch (final ExecutionException e) {
                    throw new RuntimeException("Failed to read from file", e);
                }
            }
        }

        return this;
    }

    private static List<String> readNamesFromFile(final Path file) {
        final List<String> names = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            for (String line; (line = reader.readLine()) != null; ) {
                names.add(line);
            }
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read names from file: %s".formatted(file), e);
        }

        return names;
    }

    private static List<Path> listFiles(final Path directory) {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list files in directory: %s".formatted(directory), e);
        }
    }

    public int size() {
        return this.names.size();
    }
}
