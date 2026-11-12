package demo;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;

import static demo.utils.Logs.log;

public final class ThreadLocalRandomExample {

    private static final Random SHARED_RANDOM = new Random();

    public static void run() {
        final int numberOfWorkers = 2;

        /* Only in Java 19 the ExecutorService implements AutoClosable! */
        final ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        try {
            log("A shared Random is thread-safe, but both workers update one generator");
            generateRandomNumbers(executor, "Shared Random", () -> SHARED_RANDOM.nextInt(100) + 1, numberOfWorkers);

            log("ThreadLocalRandom gives each worker random state without sharing this generator");
            generateRandomNumbers(executor, "ThreadLocalRandom",
                    () -> ThreadLocalRandom.current().nextInt(1, 101), numberOfWorkers);
        } finally {
            executor.shutdown();
        }

        log("Done");
    }

    private static void generateRandomNumbers(final ExecutorService executor,
                                              final String generatorName,
                                              final IntSupplier nextRandomNumber,
                                              final int numberOfWorkers) {
        CompletableFuture.allOf(
                        IntStream.range(0, numberOfWorkers)
                                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                                    final int randomNumber = nextRandomNumber.getAsInt();
                                    log(generatorName + " generated random number: " + randomNumber);
                                }, executor))
                                .toArray(CompletableFuture[]::new))
                .join();
    }
}
