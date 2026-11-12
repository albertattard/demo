package demo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logHeaderAndJavaVersion;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("Concurrent Linked Queue");

        /* The number of items to be added to the queue, and then polled */
        final int range = 10;

        log("Check FIFO order using a synchronized LinkedList");
        final List<Integer> linkedListFifoOrder = loadUnloadInOrder(new LinkedListIntQueue(), range);

        log("Check FIFO order using a ConcurrentLinkedQueue");
        final List<Integer> concurrentLinkedQueueFifoOrder = loadUnloadInOrder(new ConcurrentLinkedIntQueue(), range);

        log("FIFO order:");
        log(" LinkedList + synchronized: " + linkedListFifoOrder);
        log(" ConcurrentLinkedQueue:     " + concurrentLinkedQueueFifoOrder);

        log("Load and unload numbers concurrently using a synchronized LinkedList");
        final List<Integer> unloadedLinkedList = loadUnloadUsingQueueThreadPerOperation(new LinkedListIntQueue(), range);

        log("Load and unload numbers concurrently using a ConcurrentLinkedQueue");
        final List<Integer> unloadedConcurrentLinkedQueue = loadUnloadUsingQueueThreadPerOperation(new ConcurrentLinkedIntQueue(), range);

        log("Concurrent results (sorted to compare membership):");
        log(" LinkedList + synchronized: " + unloadedLinkedList);
        log(" ConcurrentLinkedQueue:     " + unloadedConcurrentLinkedQueue);
    }

    private static List<Integer> loadUnloadInOrder(final IntQueue queue, final int range) {
        IntStream.range(0, range).forEach(queue::add);
        return IntStream.range(0, range)
                .map(ignored -> queue.poll())
                .boxed()
                .collect(Collectors.toList());
    }

    private static List<Integer> loadUnloadUsingQueueThreadPerOperation(final IntQueue queue, final int range) {
        /* The queue is loaded by the given number of values and then unloaded again using a thread per operation, thus,
            we need two times the given number of threads. */
        /* Only in Java 19 the ExecutorService implements AutoClosable! */
        final ExecutorService executor = Executors.newFixedThreadPool(range * 2);
        try {
            /* Load the values to the queue using a thread per value */
            final List<CompletableFuture<Void>> loaders = IntStream.range(0, range)
                    .mapToObj(number -> CompletableFuture.runAsync(() -> queue.add(number), executor))
                    .collect(Collectors.toList());

            /* Complete every add before polling. poll() does not wait for an item when the queue is empty. */
            CompletableFuture.allOf(loaders.toArray(new CompletableFuture<?>[0])).join();

            /* Unload the values from the queue using a thread per request/value. */
            final List<CompletableFuture<Integer>> unloaders = IntStream.range(0, range)
                    .mapToObj(number -> CompletableFuture.supplyAsync(queue::poll, executor))
                    .collect(Collectors.toList());

            /* Wait for all threads to complete */
            CompletableFuture.allOf(unloaders.toArray(new CompletableFuture<?>[0])).join();

            /* Return the unloaded values, which must be between 0 and the given range */
            return unloaders.stream()
                    .map(CompletableFuture::join)
                    .sorted()
                    .collect(Collectors.toList());
        } finally {
            executor.shutdown();
        }
    }
}
