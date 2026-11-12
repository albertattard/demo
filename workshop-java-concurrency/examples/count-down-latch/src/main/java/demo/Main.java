package demo;

import java.util.concurrent.CountDownLatch;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logError;
import static demo.utils.Logs.logHeaderAndJavaVersion;
import static demo.utils.Threads.sleepForTwoSeconds;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("CountDownLatch");

        /* Initialize CountDownLatch with a count of 3 */
        final CountDownLatch latch = new CountDownLatch(3);

        // Create and start three worker threads
        for (int i = 1; i <= 3; i++) {
            final Runnable task = () -> {
                log("Task is performing some work before signaling its completion");

                try {
                    /* Simulate some work with the resource */
                    sleepForTwoSeconds();
                    log("Task completed; counting down the latch.");
                } finally {
                    latch.countDown();
                }
            };

            new Thread(task, "Thread-" + i).start();
        }

        log("Main thread is waiting for tasks to finish...");
        try {
            latch.await(); /* Main thread waits until the latch count reaches zero */
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            logError("Main thread interrupted while waiting for the other threads to complete.", e);
        }
        log("All workers have signalled completion. Main thread can proceed.");
    }
}
