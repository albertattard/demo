package demo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logHeaderAndJavaVersion;
import static demo.utils.Threads.sleepForAFewMilliseconds;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("ReentrantLock Condition");

        final SharedQueue queue = new SharedQueue(5);
        final ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            log("Create Producer");
            final CompletableFuture<Void> producer = CompletableFuture.runAsync(() ->
                    IntStream.concat(IntStream.rangeClosed(1, 10), IntStream.of(NO_MORE_ENTRIES) /* Used to signal to the consumer that there are not more elements */)
                            .peek(i -> {if (i > 6) {sleepForAFewMilliseconds();}})
                            .forEach(queue::produce), executor);

            log("Create Consumer");
            final CompletableFuture<Void> consumer = CompletableFuture.runAsync(() -> {
                /* Keep reading from the queue until the NO_MORE_ENTRIES is read */
                for (int i; (i = queue.consume()) != NO_MORE_ENTRIES; ) {
                    log("Consumed " + i + " from the queue");
                }
            }, executor);

            producer.join();
            consumer.join();
            log("Completed");
        } finally {
            executor.shutdownNow();
        }
    }

    private static final int NO_MORE_ENTRIES = -1;
}
