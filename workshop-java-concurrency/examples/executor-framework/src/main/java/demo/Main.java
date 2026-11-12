package demo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static demo.utils.Logs.*;
import static demo.utils.Threads.sleepFor;
import static demo.utils.Threads.sleepForAFewSeconds;

public final class Main {

    public static void main(final String[] args) throws ExecutionException, InterruptedException {
        logHeaderAndJavaVersion("Executor Framework");

        logSection("Create ExecutorService (newFixedThreadPool())");
        log("Use newFixedThreadPool() to create an executor service using a fixed thread pool");
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {

            final Runnable runnable = () -> {
                log("Running task 1 (Runnable)");
                sleepForAFewSeconds();
                log("Task 1 completed");
            };
            final Future<?> future1 = executor.submit(runnable);

            final Callable<Long> callable = () -> {
                log("Running task 2 (Callable)");
                sleepForAFewSeconds();
                log("Task 2 completed");
                return 42L;
            };
            final Future<Long> future2 = executor.submit(callable);

            final Future<String> future3 = executor.submit(() -> {
                log("Running task 3 (Runnable+Result)");
                sleepForAFewSeconds();
                log("Task 3 completed");
            }, "Returned once the task completes");

            final Runnable slowRunnable = () -> {
                log("Running (slow) task 4");
                try {
                    TimeUnit.SECONDS.sleep(10);
                    log("Task 4 completed");
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log("Task 4 interrupted");
                }
            };
            final Future<?> future4 = executor.submit(slowRunnable);

            log("Waiting for the futures to finish");

            /* The runnable will return null */
            final Object result1 = future1.get();
            log("The result from the runnable is: '" + result1 + '\'');

            final Object result2 = future2.get();
            log("The result from the callable is: '" + result2 + '\'');

            final Object result3 = future3.get();
            log("The result from the runnable+result is: '" + result3 + '\'');

            try {
                future4.get(3, TimeUnit.SECONDS);
                log("Task 4 completed in time");
            } catch (final TimeoutException e) {
                log("Task 4 timed out; the task is still running");
                final boolean cancelled = future4.cancel(true);
                log("Requested cancellation of task 4: " + cancelled);
            }

            log("The first three futures completed");
        }


        logSection("Create ScheduledExecutorService (newScheduledThreadPool()))");
        log("Use newScheduledThreadPool() to create a scheduled executor service using a fixed thread pool");
        try (ScheduledExecutorService executor = Executors.newScheduledThreadPool(2)) {
            log("Schedule task every 2 seconds");
            final ScheduledFuture<?> future = executor.scheduleAtFixedRate(
                    () -> log("Running task"), 1, 2, TimeUnit.SECONDS);

            sleepFor(Duration.ofSeconds(5));
            future.cancel(true);
            try {
                future.get();
            } catch (final CancellationException e) {
                log("Task cancelled before it completed");
            }
        }


        logSection("Executor Service (newVirtualThreadPerTaskExecutor())");
        log("Use newVirtualThreadPerTaskExecutor() to create a executor service that uses virtual threads");
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            final Path directory = Path.of("./src");
            log("Computing SHA-256 for all files under the '" + directory + "' directory");

            /* Collect futures for all file processing tasks */
            final List<? extends Future<?>> futures;
            try (Stream<Path> files = Files.walk(directory)) {
                futures = files.filter(Files::isRegularFile) // Only process files
                        .map(file -> executor.submit(() -> computeSha256(file)))
                        .toList();
            } catch (final IOException e) {
                throw new UncheckedIOException("Failed to walk directory", e);
            }

            /* Wait for all tasks to complete */
            for (final Future<?> future : futures) {
                future.get();
            }
            log("All file hashes computed.");
        }


        logSection("Execute Multiple Tasks (invokeAll())");
        log("Use invokeAll() to execute multiple callables and wait for all to complete.");
        try (ExecutorService executor = Executors.newFixedThreadPool(3)) {
            final List<Callable<String>> tasks = List.of(
                    () -> {
                        sleepForAFewSeconds();
                        return "Task A";
                    },
                    () -> {
                        sleepForAFewSeconds();
                        return "Task B";
                    },
                    () -> {
                        sleepForAFewSeconds();
                        return "Task C";
                    });

            log("Invoke all tasks");
            final List<Future<String>> results = executor.invokeAll(tasks);
            for (final Future<String> result : results) {
                log("Task completed with result: " + result.get());
            }
        }
    }

    private static void computeSha256(final Path file) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream dis = new DigestInputStream(new BufferedInputStream(Files.newInputStream(file)), digest)) {
                final byte[] buffer = new byte[8192];
                while (dis.read(buffer) != -1) ; /* Process file in chunks */
            }
            final String hashHex = HexFormat.of().formatHex(digest.digest());
            log("File: " + file + " -> " + hashHex);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read file", e);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
