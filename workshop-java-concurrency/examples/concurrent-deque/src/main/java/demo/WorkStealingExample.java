package demo;

import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static demo.utils.Logs.log;
import static java.util.Objects.requireNonNull;

public final class WorkStealingExample {

    static void run() {
        final Deque<SlowTask> slowDeque = new ConcurrentLinkedDeque<>();
        IntStream.rangeClosed(1, 5).mapToObj(SlowTask::new).forEach(slowDeque::add);

        final Deque<QuickTask> quickDeque = new ConcurrentLinkedDeque<>();
        IntStream.rangeClosed(1, 5).mapToObj(QuickTask::new).forEach(quickDeque::add);

        final WorkStealingWorker slowWorker = new WorkStealingWorker(slowDeque, quickDeque);
        final WorkStealingWorker quickWorker = new WorkStealingWorker(quickDeque, slowDeque);

        log("Running both workers using two threads");
        log("Slow deque:  " + slowDeque);
        log("Quick deque: " + quickDeque);
        /* Only in Java 19 the ExecutorService implements AutoClosable! */
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture.allOf(
                            CompletableFuture.runAsync(slowWorker, executor),
                            CompletableFuture.runAsync(quickWorker, executor))
                    .join();
        } finally {
            executor.shutdown();
        }

        log("Both workers are complete");
        log("Slow deque:  " + slowDeque);
        log("Quick deque: " + quickDeque);
    }

    static final class WorkStealingWorker implements Runnable {

        private final Deque<? extends Runnable> localDeque;
        private final Deque<? extends Runnable> victimDeque;

        WorkStealingWorker(final Deque<? extends Runnable> localDeque, final Deque<? extends Runnable> victimDeque) {
            this.localDeque = requireNonNull(localDeque, "The local deque cannot be null");
            this.victimDeque = requireNonNull(victimDeque, "The victim deque cannot be null");
        }

        @Override
        public void run() {
            while (true) {
                log("Taking the newest task from this worker’s deque (pollLast())");
                Runnable task = localDeque.pollLast();

                if (task != null) {
                    log("Running the task fetched from this worker’s deque");
                } else {
                    log("No local tasks available. Trying to steal the oldest task from the other worker’s deque (pollFirst())");
                    task = victimDeque.pollFirst();

                    if (task == null) {
                        log("No more tasks are available");
                        return;
                    }

                    log("Running the task stolen from the other thread’s deque.");
                }

                task.run();
            }
        }
    }
}
