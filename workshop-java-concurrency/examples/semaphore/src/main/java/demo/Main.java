package demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logHeaderAndJavaVersion;
import static demo.utils.Threads.sleepForTwoSeconds;
import static demo.utils.Threads.waitForAllThreadsToComplete;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("Semaphores");

        final int numberOfWorkers = 5;
        final int numberOfAllowedPermits = 2;

        /* Allow at most two (numberOfAllowedPermits) tasks to use the simulated
            resource concurrently. */
        final Semaphore semaphore = new Semaphore(numberOfAllowedPermits);

        final List<Thread> threads = new ArrayList<>(numberOfWorkers);
        for (int i = 1; i <= numberOfWorkers; i++) {
            final Runnable task = () -> {
                try {
                    log("Task is trying to acquire a permit.");
                    semaphore.acquire();
                    try {
                        log("Task acquired a permit.");

                        /* Simulate some work with the resource */
                        sleepForTwoSeconds();
                    } finally {
                        log("Task releasing the permit.");
                        semaphore.release();
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log("Task was interrupted while acquiring the semaphore.");
                }
            };

            final Thread thread = new Thread(task, "Thread-" + i);
            threads.add(thread);
            thread.start();
        }

        log("Wait for all threads to complete");
        waitForAllThreadsToComplete(threads);
        log("Done");
    }
}
