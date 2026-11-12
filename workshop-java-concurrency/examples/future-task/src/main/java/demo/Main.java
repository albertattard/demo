package demo;

import java.util.concurrent.*;

import static demo.utils.CountDownLatches.await;
import static demo.utils.Logs.*;
import static demo.utils.Threads.sleepForAFewSeconds;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("FutureTask");

        /* Create a FutureTask with a Callable */
        logSection("Run a FutureTask");
        log("Use Callable to run a future task and obtain the result");
        final Callable<Integer> callable = () -> {
            log("Running...");
            sleepForAFewSeconds();
            return 42;
        };
        final FutureTask<Integer> futureTask1 = new FutureTask<>(callable);
        new Thread(futureTask1, "FUTURE_TASK_1").start();

        try {
            final int value = futureTask1.get();
            log("The value returned by future task 1 is: " + value);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for future task 1 to complete", e);
        } catch (final ExecutionException e) {
            throw new RuntimeException("Future task 1 failed", e);
        }


        /* Handle errors */
        logSection("Handle errors");
        log("Handle ExecutionException when the task fails (throws an exception)");
        final FutureTask<Integer> futureTask2 = new FutureTask<>(() -> {
            log("Running...");
            throw new Exception("Simulating errors!!");
        });
        new Thread(futureTask2, "FUTURE_TASK_2").start();

        try {
            final int value = futureTask2.get();
            log("Future task 2 will always fail and it will not return a value (" + value + "), thus this message should not be printed!");
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for future task 2 to complete", e);
        } catch (final ExecutionException e) {
            log("Failed while running future task 2 (" + e.getMessage() + ')');
        }


        /* Cancel long running tasks */
        logSection("Cancel tasks");
        log("Handle CancellationException when the task is cancelled");
        final CountDownLatch futureTask3Started = new CountDownLatch(1);
        final FutureTask<Integer> futureTask3 = new FutureTask<>(() -> {
            log("Running...");
            futureTask3Started.countDown();
            TimeUnit.SECONDS.sleep(20);
            return 42;
        });
        new Thread(futureTask3, "FUTURE_TASK_3").start();

        await(futureTask3Started);

        log("Cancelling future task 3");
        futureTask3.cancel(true);

        try {
            final int value = futureTask3.get();
            log("Future task 3 will always fail and it will not return a value (" + value + "), thus this message should not be printed!");
        } catch (final CancellationException e) {
            log("Cancelled while running future task 3 (Thread interrupted: " + Thread.currentThread().isInterrupted() + ')');
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for future task 3 to complete", e);
        } catch (final ExecutionException e) {
            throw new RuntimeException("Future task 3 failed", e);
        }


        /* Interrupt long running tasks */
        logSection("Interrupt tasks");
        log("Handle ExecutionException when the thread is interrupted");
        final CountDownLatch futureTask4Started = new CountDownLatch(1);
        final FutureTask<Integer> futureTask4 = new FutureTask<>(() -> {
            log("Running...");
            futureTask4Started.countDown();
            TimeUnit.SECONDS.sleep(20);
            return 42;
        });
        final Thread thread4 = new Thread(futureTask4, "FUTURE_TASK_4");
        thread4.start();

        await(futureTask4Started);

        log("Interrupting future task 4");
        thread4.interrupt();

        try {
            final int value = futureTask4.get();
            log("Future task 4 will always fail and it will not return a value (" + value + "), thus this message should not be printed!");
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for future task 4 to complete", e);
        } catch (final ExecutionException e) {
            /* Interruptions and wrapped within the ExecutionException */
            log("Failed while running future task 4 (" + e.getMessage() + " - Thread interrupted: " + Thread.currentThread().isInterrupted() + ')');
        }


        /* Interrupt while retrieving a result */
        logSection("Interrupt while retrieving a result");
        log("Handle InterruptedException when the thread retrieving a FutureTask result is interrupted");
        final CountDownLatch futureTask5Started = new CountDownLatch(1);
        final FutureTask<Integer> futureTask5 = new FutureTask<>(() -> {
            log("Running...");
            futureTask5Started.countDown();
            TimeUnit.SECONDS.sleep(20);
            return 42;
        });
        final Thread thread5 = new Thread(futureTask5, "FUTURE_TASK_5");
        thread5.start();
        await(futureTask5Started);

        /* Wait for the value within another thread, which we will then interrupt */
        final CountDownLatch getValueThreadReady = new CountDownLatch(1);
        final Thread getValueThread = new Thread(() -> {
            getValueThreadReady.countDown();
            try {
                final int value = futureTask5.get();
                log("Future task 5 will always fail and it will not return a value (" + value + "), thus this message should not be printed!");
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                log("Interrupted while waiting the future task 5 to complete (as expected)");
            } catch (final ExecutionException e) {
                /* Interruptions and wrapped within the ExecutionException */
                throw new RuntimeException("Future task 5 failed", e);
            }
        });
        getValueThread.start();

        await(getValueThreadReady);

        log("Interrupt the thread retrieving the future task 5 result");
        getValueThread.interrupt();
        try {
            log("Waiting for the value thread 5 to complete");
            getValueThread.join();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for the thread to complete", e);
        }

        log("Cancelling future task 5");
        futureTask5.cancel(true);
        try {
            log("Waiting for future task 5 to complete");
            thread5.join();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for future task 5 to complete", e);
        }

        log("Done");
    }
}
