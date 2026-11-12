package demo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.IntStream;

import static demo.utils.CountDownLatches.await;
import static demo.utils.Logs.log;
import static demo.utils.Numbers.requirePositive;
import static java.util.Objects.requireNonNull;

public final class AtomicIntegerArrayExample {

    private static final int NUMBER_OF_WORKERS = 8;

    public static void run() {
        final int numberOfIncrements = 1_000;

        final Counter[] counters = {
                new NaiveCounter(1),
                new SyncCounter(1),
                new AtomicCounter(1)};

        for (final Counter counter : counters) {
            log("Incrementing " + counter.caption() + " " + numberOfIncrements + " times using " + NUMBER_OF_WORKERS + " workers");
            incrementCounterConcurrently(numberOfIncrements, counter);

            final int value = counter.valueAt(0);
            if (value == numberOfIncrements) {
                log(counter.caption() + " value at index 0 is " + value + " as expected");
            } else {
                log(counter.caption() + " value at index 0 is " + value + " is not as expected (" + numberOfIncrements + "); lost updates are visible");
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
                        counter.incrementAt(0);
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
        void incrementAt(int index);

        int valueAt(int index);

        default String caption() {
            return getClass().getSimpleName();
        }
    }

    static final class AtomicCounter implements Counter {

        private final AtomicIntegerArray counter;

        AtomicCounter(final int size) {
            this.counter = new AtomicIntegerArray(size);
        }

        @Override
        public void incrementAt(final int index) {
            counter.incrementAndGet(index);
        }

        @Override
        public int valueAt(final int index) {
            return counter.get(index);
        }
    }

    static final class SyncCounter implements Counter {

        private final int[] counter;

        SyncCounter(final int size) {
            this.counter = new int[size];
        }

        @Override
        public synchronized void incrementAt(final int index) {
            counter[index]++;
        }

        @Override
        public synchronized int valueAt(final int index) {
            return counter[index];
        }
    }

    static final class NaiveCounter implements Counter {

        private final int[] counter;

        NaiveCounter(final int size) {
            this.counter = new int[size];
        }

        @Override
        public void incrementAt(final int index) {
            counter[index]++;
        }

        @Override
        public int valueAt(final int index) {
            return counter[index];
        }
    }
}
