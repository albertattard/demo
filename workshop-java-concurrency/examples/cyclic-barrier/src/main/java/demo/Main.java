package demo;

import demo.utils.Logs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static demo.utils.Logs.log;
import static demo.utils.Threads.sleepForAFewSeconds;
import static demo.utils.Threads.waitForAllThreadsToComplete;

public final class Main {

    public static void main(final String[] args) {
        Logs.logHeaderAndJavaVersion("CyclicBarrier");

        /* Create one CyclicBarrier for 3 threads and a barrier action */
        final CyclicBarrier barrier = new CyclicBarrier(3, () ->
                log("All threads have reached the barrier. Releasing them for the next phase.")
        );

        /* Create and start 3 threads simulating team members */
        final List<Thread> threads = new ArrayList<>(3);
        for (int i = 1; i <= 3; i++) {
            final Runnable task = () -> {
                for (int phase = 1; phase <= 2; phase++) {
                    /* Simulate work for this phase */
                    log("Phase " + phase + ": task performing some work");
                    sleepForAFewSeconds();

                    try {
                        log("Phase " + phase + ": task is ready and waiting for the other threads.");
                        barrier.await(); /* The same barrier waits for all threads in each phase */
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted while waiting for the other threads to reach this point.", e);
                    } catch (final BrokenBarrierException e) {
                        throw new RuntimeException("Broken barrier", e);
                    }

                    log("Phase " + phase + ": all threads made it to this point");
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
