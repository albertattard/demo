package demo;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logError;
import static demo.utils.CountDownLatches.await;
import static demo.utils.Threads.sleepForAFewMilliseconds;
import static java.util.Objects.requireNonNull;

public final class CopyOnWriteArraySetExample {

    public static void run() {
        log("Concurrent read and write entries to HashSet");
        log("This access pattern is unsafe; its iterator may fail fast");
        concurrentReadAndWrite(HashSet::new);

        log("Concurrent read and write entries to CopyOnWriteArraySet");
        concurrentReadAndWrite(CopyOnWriteArraySet::new);
    }

    private static void concurrentReadAndWrite(final Supplier<Set<String>> setConstructor) {
        requireNonNull(setConstructor, "The set constructor cannot be null");

        final Set<String> cities = setConstructor.get();
        cities.add("Vienna (Austria)");
        cities.add("Brussels (Belgium)");
        cities.add("Paris (France)");
        cities.add("Berlin (Germany)");
        cities.add("Dublin (Ireland)");
        cities.add("Vaduz (Liechtenstein)");
        cities.add("Luxembourg City (Luxembourg)");
        cities.add("Monaco (Monaco)");
        cities.add("Amsterdam (Netherlands)");
        cities.add("Bern (Switzerland)");

        final CountDownLatch firstSnapshotTaken = new CountDownLatch(1);
        final CountDownLatch firstCityAdded = new CountDownLatch(1);
        final CountDownLatch secondSnapshotTaken = new CountDownLatch(1);

        /* Only in Java 19 the ExecutorService implements AutoClosable! */
        final ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            /* This reader takes a snapshot of the original 10 cities. */
            final CompletableFuture<Void> reader1 = CompletableFuture.runAsync(() -> {
                        log("Printing Cities (->):");
                        final Iterator<String> snapshot = cities.iterator();
                        firstSnapshotTaken.countDown();
                        while (snapshot.hasNext()) {
                            final String city = snapshot.next();
                            log(" -> " + city);
                            sleepForAFewMilliseconds();
                        }
                    }, executor)
                    .exceptionally(throwable -> {
                        logError("Failed to read the list of cities (->)", throwable);
                        return null;
                    });

            final CompletableFuture<Void> writer = CompletableFuture.runAsync(() -> {
                final String[] moreCities = {
                        "Copenhagen (Denmark)",
                        "Tallinn (Estonia)",
                        "Helsinki (Finland)",
                        "Reykjavik (Iceland)",
                        "Riga (Latvia)",
                        "Vilnius (Lithuania)",
                        "Oslo (Norway)",
                        "Stockholm (Sweden)",
                        "London (United Kingdom)"};

                log("Adding Cities (++):");
                await(firstSnapshotTaken);

                log(" ++ " + moreCities[0]);
                cities.add(moreCities[0] + '*'); /* Mark the new cities with a '*' */
                firstCityAdded.countDown();

                await(secondSnapshotTaken);
                for (int i = 1; i < moreCities.length; i++) {
                    final String city = moreCities[i];
                    log(" ++ " + city);
                    cities.add(city + '*'); /* Mark the new cities with a '*' */
                    sleepForAFewMilliseconds();
                }
            }, executor);

            /* This reader takes a snapshot containing the first added city. */
            final CompletableFuture<Void> reader2 = CompletableFuture.runAsync(() -> {
                        log("Printing Cities (=>):");
                        await(firstCityAdded);
                        final Iterator<String> snapshot = cities.iterator();
                        secondSnapshotTaken.countDown();
                        while (snapshot.hasNext()) {
                            final String city = snapshot.next();
                            log(" => " + city);
                            sleepForAFewMilliseconds();
                        }
                    }, executor)
                    .exceptionally(throwable -> {
                        logError("Failed to read the list of cities (=>)", throwable);
                        return null;
                    });

            /* Wait for all tasks to complete */
            CompletableFuture.allOf(writer, reader1, reader2).join();

            log("Total number of cities: " + cities.size());
        } finally {
            executor.shutdown();
        }
    }
}
