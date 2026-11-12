package demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

@Timeout(value = 20, threadMode = SEPARATE_THREAD)
class SharedQueueTest {

    @Test
    void producerBlocksWhenQueueIsFull() {
        withExecutor(2, executor -> {
            /* The class that is being tested */
            final SharedQueue queue = new SharedQueue(3);

            /* Fill the queue to its maximum capacity */
            IntStream.rangeClosed(1, queue.capacity()).forEach(queue::produce);

            /* Try to produce another item */
            final AtomicReference<Thread> producerThread = new AtomicReference<>();
            final CompletableFuture<Void> producer = CompletableFuture.runAsync(() -> {
                producerThread.set(Thread.currentThread());
                queue.produce(99); /* This should block until space is available */
            }, executor);

            /* Ensure producer is still blocked */
            awaitThreadToPark(producerThread, "Producer should be parked while the queue is full");
            assertThat(producer.isDone())
                    .describedAs("Producer should be blocked")
                    .isFalse();

            /* Consume an item to free space */
            queue.consume();

            /* Wait for producer to complete */
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
            awaitThreadToPark(consumerThread, "Consumer should be parked while the queue is empty");
            assertThat(consumer.isDone())
                    .describedAs("Consumer should be blocked")
                    .isFalse();

            /* Produce an item to unblock the consumer */
            queue.produce(42);

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
            final CompletableFuture<Void> producer = CompletableFuture.runAsync(() -> produced.forEach(queue::produce), executor);

            /* Consume elements and collect them */
            final List<Integer> consumed = CompletableFuture.supplyAsync(
                            () -> IntStream.range(0, produced.size())
                                    .mapToObj(i -> queue.consume())
                                    .collect(Collectors.toList())
                            , executor)
                    .join();

            assertThat(consumed)
                    .describedAs("Elements should be consumed in FIFO order")
                    .isEqualTo(produced);
            producer.join();
        });
    }

    @Test
    void singleProducerSingleConsumer() {
        withExecutor(2, executor -> {
            /* The class that is being tested */
            final SharedQueue queue = new SharedQueue(3);

            /* Create a single producer */
            final CompletableFuture<Void> producer = CompletableFuture.runAsync(() -> queue.produce(1), executor);

            /* Create a single consumer */
            final CompletableFuture<Integer> consumer = CompletableFuture.supplyAsync(queue::consume, executor);

            assertThat(consumer.join())
                    .describedAs("Consumer should consume the correct value.")
                    .isEqualTo(1);
            producer.join();
            assertThat(queue.isEmpty()).isTrue();
        });
    }

    @Test
    void multipleProducerSingleConsumer() {
        final int numberOfProducers = 10;

        withExecutor(numberOfProducers + 1, executor -> {
            /* The class that is being tested */
            final SharedQueue queue = new SharedQueue(3);

            /* Start many producers */
            final List<CompletableFuture<Void>> producers = IntStream.rangeClosed(1, numberOfProducers)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> queue.produce(i), executor))
                    .collect(Collectors.toList());

            /* Start a single consumer */
            final CompletableFuture<Set<Integer>> consumer = CompletableFuture.supplyAsync(() -> {
                final Set<Integer> consumed = new HashSet<>(numberOfProducers);
                IntStream.rangeClosed(1, numberOfProducers).forEach(i -> consumed.add(queue.consume()));
                return consumed;
            }, executor);

            assertThat(consumer.join())
                    .describedAs("Consumer should consume the correct value.")
                    .isEqualTo(IntStream.rangeClosed(1, numberOfProducers).boxed().collect(Collectors.toCollection(() -> new HashSet<>(numberOfProducers))));
            producers.forEach(CompletableFuture::join);
            assertThat(queue.isEmpty()).isTrue();
        });
    }

    @Test
    void singleProduceMultipleConsumers() {
        final int numberOfConsumers = 10;

        withExecutor(numberOfConsumers + 1, executor -> {
            /* The class that is being tested */
            final SharedQueue queue = new SharedQueue(3);

            /* Start a single producer */
            final CompletableFuture<Void> producer = CompletableFuture.runAsync(() ->
                    IntStream.rangeClosed(1, numberOfConsumers).forEach(queue::produce), executor);

            /* Start many consumers consumer */
            final Set<Integer> collected = IntStream.rangeClosed(1, numberOfConsumers)
                    .mapToObj(i -> CompletableFuture.supplyAsync(queue::consume, executor))
                    .map(CompletableFuture::join)
                    .collect(Collectors.toCollection(() -> new HashSet<>(numberOfConsumers)));

            assertThat(collected)
                    .describedAs("Consumer should consume the correct value.")
                    .isEqualTo(IntStream.rangeClosed(1, numberOfConsumers).boxed().collect(Collectors.toCollection(() -> new HashSet<>(numberOfConsumers))));
            producer.join();
            assertThat(queue.isEmpty()).isTrue();
        });
    }

    @Test
    void stressTestMultipleProducersAndConsumers() {
        final int numberOfEachTypeOfParticipants = 1000;

        withExecutor(numberOfEachTypeOfParticipants * 2, executor -> {
            /* The class that is being tested */
            final SharedQueue queue = new SharedQueue(3);

            /* Start multiple producers */
            final List<CompletableFuture<Void>> producers = IntStream.rangeClosed(1, numberOfEachTypeOfParticipants)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> queue.produce(i), executor))
                    .collect(Collectors.toList());

            /* Start multiple consumers */
            final List<CompletableFuture<Integer>> consumers = IntStream.rangeClosed(1, numberOfEachTypeOfParticipants)
                    .mapToObj(i -> CompletableFuture.supplyAsync(queue::consume, executor))
                    .collect(Collectors.toList());

            producers.forEach(CompletableFuture::join);
            final List<Integer> consumed = consumers.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            assertThat(consumed)
                    .containsExactlyInAnyOrderElementsOf(IntStream.rangeClosed(1, numberOfEachTypeOfParticipants)
                            .boxed()
                            .collect(Collectors.toList()));
            assertThat(queue.isEmpty()).isTrue();
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

    private static void awaitThreadToPark(final AtomicReference<Thread> thread, final String description) {
        await()
                .atMost(2, TimeUnit.SECONDS)
                .alias(description)
                .until(() -> {
                    final Thread currentThread = thread.get();
                    return currentThread != null
                            && currentThread.getState() == Thread.State.WAITING;
                });
    }

    @FunctionalInterface
    private interface C<T> {
        void accept(T t) throws Exception;
    }
}
