package demo;

import demo.utils.Logs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;

import static demo.utils.Logs.log;
import static demo.utils.Threads.sleepForAFewSeconds;
import static demo.utils.Threads.waitForAllThreadsToComplete;

public final class Main {

    public static void main(final String[] args) {
        Logs.logHeaderAndJavaVersion("Phaser");

        final int numberOfThreads = 3;

        /* Create a Phaser with a number of registered party (the number of threads working with this phaser) */
        final Phaser phaser = new Phaser(numberOfThreads);

        /* Create and start the worker threads */
        final List<Thread> threads = new ArrayList<>(numberOfThreads);
        for (int i = 1; i <= numberOfThreads; i++) {
            final int taskId = i;
            final Runnable task = () -> {
                try {
                    /* Phase 1: Simulate work */
                    log("Task " + taskId + " is working on phase 1...");
                    sleepForAFewSeconds();
                    log("Task " + taskId + " completed phase 1.");
                    phaser.arriveAndAwaitAdvance(); /* Indicate completion of phase 1 */

                    /* Phase 2: Simulate work */
                    log("Task " + taskId + " is working on phase 2...");
                    sleepForAFewSeconds();
                    log("Task " + taskId + " completed phase 2.");
                    phaser.arriveAndAwaitAdvance(); /* Indicate completion of phase 2 */
                } finally {
                    phaser.arriveAndDeregister(); /* Deregister this thread */
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
