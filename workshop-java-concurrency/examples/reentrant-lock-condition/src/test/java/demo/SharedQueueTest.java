package demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

@Timeout(value = 20, threadMode = SEPARATE_THREAD)
class SharedQueueTest {

    @Test
    void rejectsZeroCapacity() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new SharedQueue(0))
                .withMessage("The capacity must be greater than zero");
    }

    @Test
    void producerBlocksWhenQueueIsFull() {
        withExecutor(2, executor -> {
            /* The class that is being tested */
            final SharedQueue queue = new SharedQueue(3);

            /* Fill the queue to its maximum capacity */
            IntStream.rangeClosed(1, queue.capacity()).forEach(queue::produce);

            /* Try to produce another item */
            final AtomicReference<Thread> producerThread = new AtomicReference<>();
            final CompletableFuture<Void> producer = CompletableFuture.runAsync(
                    () -> {
                        producerThread.set(Thread.currentThread());
                        queue.produce(99);
                        /* This should block until space is available */
                    },
                    executor);

            /* Ensure producer is still blocked */
            awaitThreadToWait(producerThread);

            /* Consume an item to free space */
            queue.consume();

            /* Wait for producer to complete */
            await()
                    .atMost(2, TimeUnit.SECONDS)
                    .until(producer::isDone);

            producer.join();
        });
    }

    @Test
    void consumerBlocksWhenQueueIsEmpty() {
        withExecutor(2, executor -> {
            /* The class that is being tested */
            final SharedQueue queue = new SharedQueue(3);

            /* Start a consumer that should block */
            final AtomicReference<Thread> consumerThread = new AtomicReference<>();
            final CompletableFuture<Integer> consumer = CompletableFuture.supplyAsync(() -> {
                consumerThread.set(Thread.currentThread());
                return queue.consume(); /* This should block */
            }, executor);

            /* Ensure consumer is still blocked */
            awaitThreadToWait(consumerThread);

            /* Produce an item to unblock the consumer */
            queue.produce(42);

            /* Wait for consumer to complete */
            await()
                    .atMost(2, TimeUnit.SECONDS)
                    .until(consumer::isDone);

            assertThat(consumer.join()).isEqualTo(42);
        });
    }

    @Test
    void fifoOrder() {
        withExecutor(2, executor -> {
            /* The class that is being tested */
            final SharedQueue queue = new SharedQueue(3);

            /* The values that the producer will produce */
            final List<Integer> produced = Collections.unmodifiableList(Arrays.asList(7, 4, 3, 9, 2));

            /* Produce multiple elements */
            CompletableFuture.runAsync(() -> produced.forEach(queue::produce), executor);

            /* Consume elements and collect them */
            final List<Integer> consumed = CompletableFuture.supplyAsync(
                            () -> IntStream.range(0, produced.size())
                                    .mapToObj(i -> queue.consume())
                                    .collect(Collectors.toList()),
                            executor)
                    .join();

            assertThat(consumed)
                    .describedAs("Elements should be consumed in FIFO order")
                    .isEqualTo(produced);
        });
    }

    @Test
    void singleProducerSingleConsumer() {
        withExecutor(2, executor -> {
            /* The class that is being tested */
            final SharedQueue queue = new SharedQueue(3);

            /* Create a single producer */
            CompletableFuture.runAsync(() -> queue.produce(1), executor);

            /* Create a single consumer */
            final CompletableFuture<Integer> consumer = CompletableFuture.supplyAsync(queue::consume, executor);

            await()
                    .atMost(2, TimeUnit.SECONDS)
                    .alias("Queue should be empty after processing.")
                    .until(queue::isEmpty);

            assertThat(consumer.join())
                    .describedAs("Consumer should consume the correct value.")
                    .isEqualTo(1);
        });
    }

    @Test
    void multipleProducerSingleConsumer() {
        final int numberOfProducers = 10;

        withExecutor(numberOfProducers + 1, executor -> {
            /* The class that is being tested */
            final SharedQueue queue = new SharedQueue(3);

            /* Start many producers */
            IntStream.rangeClosed(1, numberOfProducers).forEach(i
                    -> CompletableFuture.runAsync(() -> queue.produce(i), executor));

            /* Start a single consumer */
            final CompletableFuture<Set<Integer>> consumer = CompletableFuture.supplyAsync(() -> {
                final Set<Integer> consumed = new HashSet<>(numberOfProducers);
                IntStream.rangeClosed(1, numberOfProducers).forEach(i -> consumed.add(queue.consume()));
                return consumed;
            }, executor);

            await()
                    .atMost(2, TimeUnit.SECONDS)
                    .alias("Queue should be empty after processing.")
                    .until(queue::isEmpty);

            assertThat(consumer.join())
                    .describedAs("Consumer should consume the correct value.")
                    .isEqualTo(IntStream.rangeClosed(1, numberOfProducers).boxed().collect(Collectors.toCollection(() -> new HashSet<>(numberOfProducers))));
        });
    }

    @Test
    void singleProduceMultipleConsumers() {
        final int numberOfConsumers = 10;

        withExecutor(numberOfConsumers + 1, executor -> {
            /* The class that is being tested */
            final SharedQueue queue = new SharedQueue(3);

            /* Start a single producer */
            CompletableFuture.runAsync(()
                    -> IntStream.rangeClosed(1, numberOfConsumers).forEach(queue::produce), executor);

            /* Start many consumers consumer */
            final Set<Integer> collected = IntStream.rangeClosed(1, numberOfConsumers)
                    .mapToObj(i -> CompletableFuture.supplyAsync(queue::consume, executor))
                    .map(CompletableFuture::join)
                    .collect(Collectors.toCollection(() -> new HashSet<>(numberOfConsumers)));

            await()
                    .atMost(2, TimeUnit.SECONDS)
                    .alias("Queue should be empty after processing.")
                    .until(queue::isEmpty);

            assertThat(collected)
                    .describedAs("Consumer should consume the correct value.")
                    .isEqualTo(IntStream.rangeClosed(1, numberOfConsumers).boxed().collect(Collectors.toCollection(() -> new HashSet<>(numberOfConsumers))));
        });
    }

    @Test
    void stressTestMultipleProducersAndConsumers() {
        /*
         * Exercise sustained contention without creating a platform thread per item:
         * four producers each publish 250 distinct values, while eight consumers each
         * remove 125 values. Both sides process 1,000 items, but the executor is
         * bounded at 12 platform threads, which is practical on Java 8.
         */
        final int numberOfProducers = 4;
        final int numberOfConsumers = 8;
        final int itemsPerProducer = 250;
        final int itemsPerConsumer = 125;
        final int numberOfItems = numberOfProducers * itemsPerProducer;
        final int numberOfThreads = numberOfProducers + numberOfConsumers;

        assertThat(numberOfItems)
                .describedAs("Producers and consumers must process the same number of items.")
                .isEqualTo(numberOfConsumers * itemsPerConsumer);

        withExecutor(numberOfThreads, executor -> {
            /* The class that is being tested */
            final SharedQueue queue = new SharedQueue(3);

            final CountDownLatch participantsReady = new CountDownLatch(numberOfThreads);
            final CountDownLatch start = new CountDownLatch(1);

            /* Start multiple producers */
            final CompletableFuture<Void> producers = CompletableFuture.allOf(
                    IntStream.range(0, numberOfProducers)
                            .mapToObj(producer -> CompletableFuture.runAsync(() -> {
                                awaitStart(participantsReady, start);
                                IntStream.range(0, itemsPerProducer)
                                        .map(item -> producer * itemsPerProducer + item + 1)
                                        .forEach(queue::produce);
                            }, executor))
                            .toArray(CompletableFuture<?>[]::new));

            /* Start multiple consumers */
            final List<CompletableFuture<List<Integer>>> consumers = IntStream.range(0, numberOfConsumers)
                    .mapToObj(consumer -> CompletableFuture.supplyAsync(() -> {
                        awaitStart(participantsReady, start);
                        return IntStream.range(0, itemsPerConsumer)
                                .mapToObj(item -> queue.consume())
                                .collect(Collectors.toList());
                    }, executor))
                    .collect(Collectors.toList());

            try {
                final boolean allParticipantsAreReady = participantsReady.await(2, TimeUnit.SECONDS);
                assertThat(allParticipantsAreReady)
                        .describedAs("All producers and consumers should be ready before the stress test starts.")
                        .isTrue();
            } finally {
                /* Release workers even when the assertion fails, so executor shutdown can complete. */
                start.countDown();
            }

            CompletableFuture.allOf(
                            producers,
                            CompletableFuture.allOf(consumers.toArray(new CompletableFuture<?>[0])))
                    .get(10, TimeUnit.SECONDS);

            final Set<Integer> consumed = consumers.stream()
                    .flatMap(consumer -> consumer.join().stream())
                    .collect(Collectors.toSet());
            assertThat(consumed)
                    .describedAs("Every produced value should be consumed exactly once.")
                    .containsExactlyInAnyOrderElementsOf(
                            IntStream.rangeClosed(1, numberOfItems)
                                    .boxed()
                                    .collect(Collectors.toList()));
        });
    }

    private static void withExecutor(final int numberOfThreads, final C<Executor> consumer) {
        /* Only in Java 19 the ExecutorService implements AutoClosable! */
        final ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        try {
            consumer.accept(executor);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted", e);
        } catch (final Exception e) {
            throw new RuntimeException("Failed", e);
        } finally {
            executor.shutdown();
        }
    }

    private static void awaitThreadToWait(final AtomicReference<Thread> thread) {
        await()
                .atMost(2, TimeUnit.SECONDS)
                .until(() -> {
                    final Thread value = thread.get();
                    return value != null && value.getState() == Thread.State.WAITING;
                });
    }

    private static void awaitStart(final CountDownLatch participantsReady, final CountDownLatch start) {
        participantsReady.countDown();
        try {
            start.await();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted", e);
        }
    }

    @FunctionalInterface
    private interface C<T> {

        void accept(T t) throws Exception;
    }
}
