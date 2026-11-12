package demo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static demo.utils.CountDownLatches.await;
import static demo.utils.Logs.log;
import static demo.utils.Numbers.requirePositive;
import static java.util.Objects.requireNonNull;

public final class AtomicIntegerExample {

    private static final int NUMBER_OF_WORKERS = 8;

    public static void run() {
        final int numberOfIncrements = 1_000;

        final Counter[] counters = {
                new NaiveCounter(),
                new SyncCounter(),
                new AtomicCounter()};

        for (final Counter counter : counters) {
            log("Incrementing " + counter.caption() + " " + numberOfIncrements + " times using " + NUMBER_OF_WORKERS + " workers");
            incrementCounterConcurrently(numberOfIncrements, counter);

            if (counter.value() == numberOfIncrements) {
                log(counter.caption() + " value is " + counter.value() + " as expected");
            } else {
                log(counter.caption() + " value is " + counter.value() + " is not as expected (" + numberOfIncrements + "); lost updates are visible");
            }
        }

        log("Done");
    }

    private static void incrementCounterConcurrently(final int numberOfIncrements, final Counter counter) {
        requirePositive(numberOfIncrements, "Cannot increment the counter using a negative number of increments");
        requireNonNull(counter, "Cannot increment a null counter");

        /* Releases the available workers only after all increments have been submitted. */
        final CountDownLatch startGate = new CountDownLatch(1);

        /* Only in Java 19 the ExecutorService implements AutoClosable! */
        final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_WORKERS);
        try {
            final CompletableFuture<?>[] increments = IntStream.range(0, numberOfIncrements)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        await(startGate);
                        counter.increment();
                    }, executor))
                    .toArray(CompletableFuture[]::new);

            startGate.countDown();
            CompletableFuture.allOf(increments).join();
        } finally {
            startGate.countDown();
            executor.shutdown();
        }
    }

    interface Counter {
        void increment();

        int value();

        default String caption() {
            return getClass().getSimpleName();
        }
    }

    static final class AtomicCounter implements Counter {

        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public void increment() {
            counter.incrementAndGet();
        }

        @Override
        public int value() {
            return counter.get();
        }
    }

    static final class SyncCounter implements Counter {

        private int counter = 0;

        @Override
        public synchronized void increment() {
            counter++;
        }

        @Override
        public synchronized int value() {
            return counter;
        }
    }

    static final class NaiveCounter implements Counter {

        private int counter = 0;

        @Override
        public void increment() {
            counter++;
        }

        @Override
        public int value() {
            return counter;
        }
    }
}
