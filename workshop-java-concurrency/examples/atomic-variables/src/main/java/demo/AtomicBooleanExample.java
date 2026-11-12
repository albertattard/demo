package demo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static demo.utils.CountDownLatches.await;
import static demo.utils.Logs.log;
import static java.util.Objects.requireNonNull;

public final class AtomicBooleanExample {

    private static final int NUMBER_OF_WORKERS = 8;

    public static void run() {
        /* A blank instance that was never used from each example */
        final RunOnce naïveRunOnce = new NaïveRunOnce();
        final RunOnce atomicRunOnce = new AtomicRunOnce();

        log("Trying the naïve run once example");
        run(naïveRunOnce);

        log("Trying the atomic run once example");
        run(atomicRunOnce);

        log("Done");
    }

    private static void run(final RunOnce runOnce) {
        /* A thousand tasks try to execute the run() method. */
        final int numberOfAttempts = 1_000;

        /* Releases the available workers only after all attempts have been submitted. */
        final CountDownLatch startGate = new CountDownLatch(1);

        /* Only in Java 19 the ExecutorService implements AutoClosable! */
        final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_WORKERS);
        try {
            /* Schedule all attempts without requiring one worker thread per attempt. */
            final List<CompletableFuture<Boolean>> futures = IntStream.rangeClosed(1, numberOfAttempts)
                    .mapToObj(Task::new)
                    .map(task -> CompletableFuture.supplyAsync(() -> {
                        await(startGate);
                        return runOnce.run(task);
                    }, executor))
                    .collect(Collectors.toList());

            startGate.countDown();

            /* Count the number of tasks that were executed */
            final int counter = futures.stream()
                    .map(CompletableFuture::join)
                    .mapToInt(executed -> executed ? 1 : 0)
                    .sum();

            /* Print the number of times the tasks were executed */
            switch (counter) {
                case 0:
                    log("No tasks were executed!");
                    break;
                case 1:
                    log("One task was executed");
                    break;
                default:
                    log("More than one task was executed!");
                    break;
            }
        } finally {
            startGate.countDown();
            executor.shutdown();
        }
    }

    private interface RunOnce {
        boolean run(final Runnable task);
    }

    private static final class AtomicRunOnce implements RunOnce {
        private final AtomicBoolean hasRun = new AtomicBoolean();

        public boolean run(final Runnable task) {
            requireNonNull(task, "Cannot run a null task");

            if (hasRun.compareAndSet(false, true)) {
                task.run();
                return true;
            }

            return false;
        }
    }

    private static final class NaïveRunOnce implements RunOnce {
        private volatile boolean hasRun = false;

        public boolean run(final Runnable task) {
            requireNonNull(task, "Cannot run a null task");

            if (!hasRun) {
                hasRun = true;
                task.run();
                return true;
            }

            return false;
        }
    }

    static final class Task implements Runnable {

        private final int number;

        Task(final int number) {
            this.number = number;
        }

        @Override
        public void run() {
            log("Running task " + number);
        }

        @Override
        public String toString() {
            return "Task[number=" + number + ']';
        }
    }
}
