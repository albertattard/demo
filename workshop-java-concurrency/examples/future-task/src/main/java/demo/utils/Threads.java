package demo.utils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public final class Threads {

    public static void sleepForTwoSeconds() {
        sleepFor(Duration.ofSeconds(2));
    }

    public static void sleepForAFewSeconds() {
        sleepFor(Duration.ofSeconds(new Random().nextInt(5) + 1));
    }

    public static void sleepForAFewMilliseconds() {
        sleepFor(Duration.ofMillis(new Random().nextInt(800) + 101));
    }

    public static void sleepFor(final Duration sleep) {
        requireNonNull(sleep, "The sleep duration cannot be null");
        try {
            TimeUnit.MILLISECONDS.sleep(sleep.toMillis());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while sleeping", e);
        }
    }

    public static void waitForThreadToComplete(final Thread thread) {
        try {
            thread.join();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for " + thread.getName(), e);
        }
    }

    public static void waitForAllThreadsToComplete(final Thread... threads) {
        waitForAllThreadsToComplete(Arrays.asList(threads));
    }

    public static void waitForAllThreadsToComplete(final Iterable<Thread> threads) {
        requireNonNull(threads, "The threads to wait for cannot be null");
        for (final Thread thread : threads) {
            try {
                thread.join();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for thread " + thread + " to complete", e);
            }
        }
    }

    private Threads() {}
}
