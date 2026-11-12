package demo;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logHeaderAndJavaVersion;
import static demo.utils.Threads.sleepForAFewMilliseconds;
import static demo.utils.Threads.waitForAllThreadsToComplete;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("Synchronized - Wait and Notify");

        final SharedQueue queue = new SharedQueue(5);
        final AtomicReference<RuntimeException> failure = new AtomicReference<>();

        log("Create Producer");
        final Thread producer = new Thread(() -> {
            try {
                IntStream.concat(IntStream.rangeClosed(1, 10), IntStream.of(NO_MORE_ENTRIES) /* Used to signal to the consumer that there are not more elements */)
                        .peek(i -> {if (i > 6) {sleepForAFewMilliseconds();}})
                        .forEach(queue::produce);
            } catch (final RuntimeException e) {
                failure.compareAndSet(null, e);
                queue.close();
            }
        }, "producer");

        log("Create Consumer");
        final Thread consumer = new Thread(() -> {
            try {
                /* Keep reading from the queue until the NO_MORE_ENTRIES is read */
                for (int i; (i = queue.consume()) != NO_MORE_ENTRIES; ) {
                    log("Consumed " + i + " from the queue");
                }
            } catch (final RuntimeException e) {
                failure.compareAndSet(null, e);
                queue.close();
            }
        }, "consumer");

        producer.start();
        consumer.start();
        waitForAllThreadsToComplete(producer, consumer);
        if (failure.get() != null) {
            throw failure.get();
        }
        log("Completed");
    }

    private static final int NO_MORE_ENTRIES = -1;
}
