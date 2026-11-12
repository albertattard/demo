package demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;

@Timeout(value = 20, threadMode = SEPARATE_THREAD)
class SharedQueueTest {

    @Test
    void producerWaitsWhenQueueIsFull() {
        /* The class that is being tested */
        final SharedQueue queue = new SharedQueue(3);
        final AtomicReference<RuntimeException> failure = new AtomicReference<>();

        /* Fill the queue to its maximum capacity */
        IntStream.rangeClosed(1, queue.capacity()).forEach(queue::produce);

        /* Try to produce another item */
        final Thread producer = new Thread(() -> {
            try {
                queue.produce(99);
            } catch (final RuntimeException e) {
                failure.set(e);
            }
        }, "producer");
        producer.start();

        try {
            await()
                    .atMost(2, TimeUnit.SECONDS)
                    .alias("Producer should wait for space in the queue")
                    .until(() -> producer.getState() == Thread.State.WAITING);

            /* Consume an item to free space */
            queue.consume();

            await()
                    .atMost(2, TimeUnit.SECONDS)
                    .alias("Producer should complete after space becomes available")
                    .until(() -> !producer.isAlive());

            assertThat(failure.get())
                    .describedAs("Producer should complete successfully after space becomes available")
                    .isNull();
            assertThat(queue.consume()).isEqualTo(2);
            assertThat(queue.consume()).isEqualTo(3);
            assertThat(queue.consume()).isEqualTo(99);
        } finally {
            unblockProducer(queue, producer);
        }
    }

    @Test
    void consumerWaitsWhenQueueIsEmpty() {
        /* The class that is being tested */
        final SharedQueue queue = new SharedQueue(3);
        final AtomicReference<Integer> consumed = new AtomicReference<>();
        final AtomicReference<RuntimeException> failure = new AtomicReference<>();

        /* Start a consumer that should wait */
        final Thread consumer = new Thread(() -> {
            try {
                consumed.set(queue.consume());
            } catch (final RuntimeException e) {
                failure.set(e);
            }
        }, "consumer");
        consumer.start();

        try {
            await()
                    .atMost(2, TimeUnit.SECONDS)
                    .alias("Consumer should wait for an item in the queue")
                    .until(() -> consumer.getState() == Thread.State.WAITING);

            /* Produce an item to unblock the consumer */
            queue.produce(42);

            await()
                    .atMost(2, TimeUnit.SECONDS)
                    .alias("Consumer should complete after an item becomes available")
                    .until(() -> !consumer.isAlive());

            assertThat(failure.get())
                    .describedAs("Consumer should complete successfully after an item becomes available")
                    .isNull();
            assertThat(consumed.get()).isEqualTo(42);
        } finally {
            unblockConsumer(queue, consumer);
        }
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
                                    .collect(Collectors.toList())
                            , executor)
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

            assertThat(consumer.join())
                    .describedAs("Consumer should consume the correct value.")
                    .isEqualTo(1);
            assertThat(queue.isEmpty())
                    .describedAs("Queue should be empty after processing.")
                    .isTrue();
        });
    }

    @Test
    void multipleProducerSingleConsumer() {
        final int numberOfProducers = 10;

        withExecutor(numberOfProducers + 1, executor -> {
            /* The class that is being tested */
            final SharedQueue queue = new SharedQueue(3);

            /* Start many producers */
            IntStream.rangeClosed(1, numberOfProducers).forEach(i ->
                    CompletableFuture.runAsync(() -> queue.produce(i), executor));

            /* Start a single consumer */
            final CompletableFuture<Set<Integer>> consumer = CompletableFuture.supplyAsync(() -> {
                final Set<Integer> consumed = new HashSet<>(numberOfProducers);
                IntStream.rangeClosed(1, numberOfProducers).forEach(i -> consumed.add(queue.consume()));
                return consumed;
            }, executor);

            assertThat(consumer.join())
                    .describedAs("Consumer should consume the correct value.")
                    .isEqualTo(IntStream.rangeClosed(1, numberOfProducers).boxed().collect(Collectors.toCollection(() -> new HashSet<>(numberOfProducers))));
            assertThat(queue.isEmpty())
                    .describedAs("Queue should be empty after processing.")
                    .isTrue();
        });
    }

    @Test
    void singleProduceMultipleConsumers() {
        final int numberOfConsumers = 10;

        withExecutor(numberOfConsumers + 1, executor -> {
            /* The class that is being tested */
            final SharedQueue queue = new SharedQueue(3);

            /* Start a single producer */
            CompletableFuture.runAsync(() ->
                    IntStream.rangeClosed(1, numberOfConsumers).forEach(queue::produce), executor);

            /* Start many consumers consumer */
            final Set<Integer> collected = IntStream.rangeClosed(1, numberOfConsumers)
                    .mapToObj(i -> CompletableFuture.supplyAsync(queue::consume, executor))
                    .map(CompletableFuture::join)
                    .collect(Collectors.toCollection(() -> new HashSet<>(numberOfConsumers)));

            assertThat(collected)
                    .describedAs("Consumer should consume the correct value.")
                    .isEqualTo(IntStream.rangeClosed(1, numberOfConsumers).boxed().collect(Collectors.toCollection(() -> new HashSet<>(numberOfConsumers))));
            assertThat(queue.isEmpty())
                    .describedAs("Queue should be empty after processing.")
                    .isTrue();
        });
    }

    @Test
    void stressTestMultipleProducersAndConsumers() {
        final int numberOfProducers = 4;
        final int numberOfConsumers = 4;
        final int itemsPerProducer = 250;
        final int numberOfItems = numberOfProducers * itemsPerProducer;

        withExecutor(numberOfProducers + numberOfConsumers, executor -> {
            /* The class that is being tested */
            final SharedQueue queue = new SharedQueue(3);

            /* Start multiple producers, each with a distinct range of values */
            final List<CompletableFuture<Void>> producers = IntStream.range(0, numberOfProducers)
                    .mapToObj(producer -> CompletableFuture.runAsync(() ->
                            IntStream.range(producer * itemsPerProducer + 1, (producer + 1) * itemsPerProducer + 1)
                                    .forEach(queue::produce), executor))
                    .collect(Collectors.toList());

            /* Start multiple consumers */
            final List<CompletableFuture<List<Integer>>> consumers = IntStream.range(0, numberOfConsumers)
                    .mapToObj(consumer -> CompletableFuture.supplyAsync(() ->
                            IntStream.range(0, numberOfItems / numberOfConsumers)
                                    .mapToObj(i -> queue.consume())
                                    .collect(Collectors.toList()), executor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(Stream.concat(producers.stream(), consumers.stream())
                            .toArray(CompletableFuture<?>[]::new))
                    .join();

            final List<Integer> consumed = consumers.stream()
                    .flatMap(consumer -> consumer.join().stream())
                    .collect(Collectors.toList());

            assertThat(consumed)
                    .describedAs("Every produced value should be consumed exactly once")
                    .containsExactlyInAnyOrderElementsOf(IntStream.rangeClosed(1, numberOfItems)
                            .boxed()
                            .collect(Collectors.toList()));
        });
    }

    @Test
    void interruptingOneWaiterClosesTheQueueAndWakesOtherWaiters() {
        final SharedQueue queue = new SharedQueue(3);
        final AtomicReference<RuntimeException> interruptedFailure = new AtomicReference<>();
        final AtomicReference<RuntimeException> peerFailure = new AtomicReference<>();
        final Thread interruptedConsumer = new Thread(() -> consumeAndRecordFailure(queue, interruptedFailure), "interrupted-consumer");
        final Thread peerConsumer = new Thread(() -> consumeAndRecordFailure(queue, peerFailure), "peer-consumer");
        interruptedConsumer.start();
        peerConsumer.start();

        try {
            await()
                    .atMost(2, TimeUnit.SECONDS)
                    .alias("Both consumers should wait for an item")
                    .until(() -> interruptedConsumer.getState() == Thread.State.WAITING
                            && peerConsumer.getState() == Thread.State.WAITING);

            interruptedConsumer.interrupt();

            await()
                    .atMost(2, TimeUnit.SECONDS)
                    .alias("Closing the queue should wake both consumers")
                    .until(() -> !interruptedConsumer.isAlive() && !peerConsumer.isAlive());

            assertThat(interruptedFailure.get())
                    .hasMessage("Interrupted while waiting")
                    .hasCauseInstanceOf(InterruptedException.class);
            assertThat(peerFailure.get())
                    .hasMessage("The queue is closed");
            assertThatThrownBy(() -> queue.produce(1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("The queue is closed");
        } finally {
            queue.close();
            await()
                    .atMost(2, TimeUnit.SECONDS)
                    .until(() -> !interruptedConsumer.isAlive() && !peerConsumer.isAlive());
        }
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

            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                        throw new RuntimeException("Executor did not terminate");
                    }
                }
            } catch (final InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while shutting down the executor", e);
            }
        }
    }

    private static void unblockProducer(final SharedQueue queue, final Thread producer) {
        if (producer.isAlive()) {
            queue.consume();
            await()
                    .atMost(2, TimeUnit.SECONDS)
                    .until(() -> !producer.isAlive());
        }
    }

    private static void unblockConsumer(final SharedQueue queue, final Thread consumer) {
        if (consumer.isAlive()) {
            queue.produce(42);
            await()
                    .atMost(2, TimeUnit.SECONDS)
                    .until(() -> !consumer.isAlive());
        }
    }

    private static void consumeAndRecordFailure(final SharedQueue queue, final AtomicReference<RuntimeException> failure) {
        try {
            queue.consume();
        } catch (final RuntimeException e) {
            failure.set(e);
        }
    }

    @FunctionalInterface
    private interface C<T> {
        void accept(T t) throws Exception;
    }
}
