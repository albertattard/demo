package demo;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CountersTest {

    @Test
    void counterAtomic() {
        incrementValueCorrectlyWhenUsingSingleThread(CounterAtomic::new);
        decrementValueCorrectlyWhenUsingSingleThread(CounterAtomic::new);
        incrementValueCorrectlyWhenUsingMultipleThreads(CounterAtomic::new);
        decrementValueCorrectlyWhenUsingMultipleThreads(CounterAtomic::new);
    }

    @Test
    void counterLock() {
        incrementValueCorrectlyWhenUsingSingleThread(CounterLock::new);
        decrementValueCorrectlyWhenUsingSingleThread(CounterLock::new);
        incrementValueCorrectlyWhenUsingMultipleThreads(CounterLock::new);
        decrementValueCorrectlyWhenUsingMultipleThreads(CounterLock::new);
    }

    @Test
    void counterSync() {
        incrementValueCorrectlyWhenUsingSingleThread(CounterSync::new);
        decrementValueCorrectlyWhenUsingSingleThread(CounterSync::new);
        incrementValueCorrectlyWhenUsingMultipleThreads(CounterSync::new);
        decrementValueCorrectlyWhenUsingMultipleThreads(CounterSync::new);
    }

    @Test
    void counterVarHandle() {
        incrementValueCorrectlyWhenUsingSingleThread(CounterVarHandle::new);
        decrementValueCorrectlyWhenUsingSingleThread(CounterVarHandle::new);
        incrementValueCorrectlyWhenUsingMultipleThreads(CounterVarHandle::new);
        decrementValueCorrectlyWhenUsingMultipleThreads(CounterVarHandle::new);
    }

    static void incrementValueCorrectlyWhenUsingSingleThread(final Supplier<Counter> factory) {
        final Counter counter = factory.get();
        assertEquals(1, counter.increment());
        assertEquals(2, counter.increment());
        assertEquals(3, counter.increment());
    }

    static void decrementValueCorrectlyWhenUsingSingleThread(final IntFunction<Counter> factory) {
        final Counter counter = factory.apply(10);
        assertEquals(9, counter.decrement());
        assertEquals(8, counter.decrement());
        assertEquals(7, counter.decrement());
    }

    static void incrementValueCorrectlyWhenUsingMultipleThreads(final Supplier<Counter> factory) {
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            final int limit = 1000;
            final Counter counter = factory.get();
            final List<Future<Integer>> futures = IntStream.range(0, limit)
                    .mapToObj(i -> executor.submit(counter::increment))
                    .toList();

            futures.forEach(CountersTest::get);
            assertEquals(limit, counter.value());
        }
    }

    static void decrementValueCorrectlyWhenUsingMultipleThreads(final IntFunction<Counter> factory) {
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            final int limit = 1000;
            final Counter counter = factory.apply(limit);
            final List<Future<Integer>> futures = IntStream.range(0, limit)
                    .mapToObj(i -> executor.submit(counter::decrement))
                    .toList();

            futures.forEach(CountersTest::get);
            assertEquals(0, counter.value());
        }
    }

    private static <T> T get(final Future<T> future) {
        try {
            return future.get();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CountersTest() {}
}
