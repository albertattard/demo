package demo;

import java.util.concurrent.*;

import static demo.utils.Logs.log;
import static demo.utils.Threads.sleepForAFewMilliseconds;
import static java.util.Objects.requireNonNull;

public final class SynchronousQueueExample {

    public static void run() {
        final int numberOfElements = 10;
        final BlockingQueue<Integer> queue = new SynchronousQueue<>();

        /* Only in Java 19 the ExecutorService implements AutoClosable! */
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            final CompletableFuture<Void> producer = CompletableFuture.runAsync(() -> {
                for (int element = 1; element <= numberOfElements; element++) {
                    log("Putting element " + element);
                    putElementToQueue(element, queue);
                    log("Element " + element + " put");
                }

                log("Producer completed");
            }, executor);
            producer.whenComplete((ignored, exception) -> shutdownNowIfFailed(executor, exception));

            final CompletableFuture<Void> consumer = CompletableFuture.runAsync(() -> {
                for (int i = 0; i < numberOfElements; i++) {
                    log("Fetching the next element from the queue");
                    final int element = takeNextElementFromQueue(queue);
                    log("Fetched element: " + element + " from the queue");
                    sleepForAFewMilliseconds();
                }

                log("No more elements to process");
                log("Consumer completed");
            }, executor);
            consumer.whenComplete((ignored, exception) -> shutdownNowIfFailed(executor, exception));

            /* Wait for both the producer and consumer to complete */
            CompletableFuture.allOf(producer, consumer).join();
        } finally {
            executor.shutdown();
        }

        log("After the consumer and producer have completed, the queue contains: " + queue);
        log("Done");
    }

    private static void shutdownNowIfFailed(final ExecutorService executor, final Throwable exception) {
        if (exception != null) {
            /* Interrupt a peer that may be blocked in put() or take(). */
            executor.shutdownNow();
        }
    }

    private static int takeNextElementFromQueue(final BlockingQueue<Integer> queue) {
        requireNonNull(queue, "Cannot take elements from a null queue");

        try {
            return queue.take(); /* Blocks if the queue is empty */
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for the next element to become available", e);
        }
    }

    private static void putElementToQueue(final int element, final BlockingQueue<Integer> queue) {
        requireNonNull(queue, "Cannot put elements to a null queue");

        try {
            queue.put(element); /* Blocks if the queue is full */
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for more space to be available", e);
        }
    }
}
