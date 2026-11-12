package demo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static demo.utils.Logs.log;
import static demo.utils.Threads.sleepForAFewMilliseconds;
import static demo.utils.Threads.sleepForTwoSeconds;

public final class DeadLockAvoidanceExample {

    private static final int MAXIMUM_ATTEMPTS = 3;

    private final Lock lockA = new ReentrantLock();
    private final Lock lockB = new ReentrantLock();

    public static void run() {
        final DeadLockAvoidanceExample example = new DeadLockAvoidanceExample();

        CompletableFuture.allOf(
                        CompletableFuture.runAsync(example::acquireLockAThenLockB),
                        CompletableFuture.runAsync(example::acquireLockBThenLockA))
                .join();
    }

    public void acquireLockAThenLockB() {
        log("Acquire Lock A then Lock B");
        try {
            for (int attempt = 1; attempt <= MAXIMUM_ATTEMPTS; attempt++) {
                log("Attempt " + attempt);
                if (!lockA.tryLock(1, TimeUnit.SECONDS)) {
                    log("Failed to acquire lock A. Trying again in a couple of milliseconds");
                    sleepForAFewMilliseconds();
                    continue;
                }

                try {
                    log("Lock A acquired");
                    sleepForTwoSeconds();

                    if (!lockB.tryLock(1, TimeUnit.SECONDS)) {
                        log("Failed to acquire lock B");
                    } else {
                        try {
                            log("Both locks acquired after " + attempt + " attempts");
                            sleepForTwoSeconds();
                            return;
                        } finally {
                            lockB.unlock();
                            log("Lock B released");
                        }
                    }
                } finally {
                    lockA.unlock();
                    log("Lock A released");
                }

                log("Trying again in a couple of milliseconds");
                sleepForAFewMilliseconds();
            }
            log("Could not acquire both locks after " + MAXIMUM_ATTEMPTS + " attempts");
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while acquiring a lock", e);
        }
    }

    public void acquireLockBThenLockA() {
        log("Acquire Lock B then Lock A");
        try {
            for (int attempt = 1; attempt <= MAXIMUM_ATTEMPTS; attempt++) {
                log("Attempt " + attempt);
                if (!lockB.tryLock(1, TimeUnit.SECONDS)) {
                    log("Failed to acquire lock B. Trying again in a couple of milliseconds");
                    sleepForAFewMilliseconds();
                    continue;
                }

                try {
                    log("Lock B acquired");
                    sleepForTwoSeconds();

                    if (!lockA.tryLock(1, TimeUnit.SECONDS)) {
                        log("Failed to acquire lock A");
                    } else {
                        try {
                            log("Both locks acquired after " + attempt + " attempts");
                            sleepForTwoSeconds();
                            return;
                        } finally {
                            lockA.unlock();
                            log("Lock A released");
                        }
                    }
                } finally {
                    lockB.unlock();
                    log("Lock B released");
                }

                log("Trying again in a couple of milliseconds");
                sleepForAFewMilliseconds();
            }
            log("Could not acquire both locks after " + MAXIMUM_ATTEMPTS + " attempts");
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while acquiring a lock", e);
        }
    }
}
