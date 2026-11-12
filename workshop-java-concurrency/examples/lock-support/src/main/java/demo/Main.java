package demo;

import java.util.concurrent.CompletableFuture;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;

import static demo.utils.Logs.*;
import static demo.utils.Threads.sleepForAFewMilliseconds;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("Lock Support");

        final SharedQueue queue = new SharedQueue(5);
        runExample("Shared queue without a helper", queue::produce, queue::consume);

        final SharedQueueUsingHelper queueUsingHelper = new SharedQueueUsingHelper(5);
        runExample("Shared queue using LockSupportHelper", queueUsingHelper::produce, queueUsingHelper::consume);

        log("Completed");
    }

    private static void runExample(final String title, final IntConsumer produce, final IntSupplier consume) {
        logSection(title);
        log("Create Producer");
        final CompletableFuture<Void> producer = CompletableFuture.runAsync(() ->
                IntStream.concat(IntStream.rangeClosed(1, 10), IntStream.of(NO_MORE_ENTRIES) /* Used to signal to the consumer that there are not more elements */)
                        .peek(i -> {if (i > 6) {sleepForAFewMilliseconds();}})
                        .forEach(produce));

        log("Create Consumer");
        final CompletableFuture<Void> consumer = CompletableFuture.runAsync(() -> {
            /* Keep reading from the queue until the NO_MORE_ENTRIES is read */
            for (int i; (i = consume.getAsInt()) != NO_MORE_ENTRIES; ) {
                log("Consumed " + i + " from the queue");
            }
        });

        producer.join();
        consumer.join();
    }

    private static final int NO_MORE_ENTRIES = -1;
}
