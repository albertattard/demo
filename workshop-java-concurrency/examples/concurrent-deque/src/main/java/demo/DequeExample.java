package demo;

import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static demo.utils.Logs.log;

public final class DequeExample {

    public static void run() {
        final Deque<Runnable> deque = new ConcurrentLinkedDeque<>();

        log("Add tasks at both ends (slow tasks to the end and quick tasks to the front)");
        deque.addLast(new SlowTask(1));
        deque.addLast(new SlowTask(2));
        deque.addFirst(new QuickTask(3));
        deque.addLast(new SlowTask(4));
        deque.addFirst(new QuickTask(5));
        log("Deque before processing: " + deque);

        final int numberOfThreads = 2;
        final int numberOfTasks = deque.size();
        log("Process queue using " + numberOfThreads + " threads");
        final int completedTaskCount = processTasks(deque, numberOfThreads);

        log("Completed " + completedTaskCount + " of " + numberOfTasks + " submitted tasks");
        log("Deque after processing: " + deque);
        log("Done");
    }

    static int processTasks(final Deque<? extends Runnable> deque, final int numberOfThreads) {
        /* Only in Java 19 the ExecutorService implements AutoClosable! */
        final ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        final AtomicInteger completedTaskCount = new AtomicInteger();
        try {
            CompletableFuture.allOf(
                            IntStream.range(0, numberOfThreads)
                                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                                        log("Processing tasks from the head of the queue (pollFirst())");
                                        for (Runnable task; (task = deque.pollFirst()) != null; ) {
                                            task.run();
                                            completedTaskCount.incrementAndGet();
                                        }
                                        log("No more tasks available");
                                    }, executor))
                                    .toArray(CompletableFuture[]::new))
                    .join();
        } finally {
            executor.shutdown();
        }

        return completedTaskCount.get();
    }
}
