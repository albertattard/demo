package demo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicStampedReference;

import static demo.utils.CountDownLatches.await;
import static demo.utils.Logs.log;
import static demo.utils.Threads.waitForAllThreadsToComplete;

public final class AtomicStampedReferenceExample {

    private static final String INITIAL_VALUE = "A";
    private static final String INTERMEDIATE_VALUE = "B";
    private static final String STALE_UPDATE_VALUE = "C";
    private static final int INITIAL_STAMP = 0;

    public static void run() {
        final AtomicStampedReference<String> value = new AtomicStampedReference<>(INITIAL_VALUE, INITIAL_STAMP);
        final CountDownLatch staleReadComplete = new CountDownLatch(1);
        final CountDownLatch abaComplete = new CountDownLatch(1);

        final Thread staleUpdater = new Thread(() -> {
            final int[] stampHolder = new int[1];
            final String expectedValue = value.get(stampHolder);
            final int expectedStamp = stampHolder[0];
            log("Stale updater read " + expectedValue + " with stamp " + expectedStamp);
            staleReadComplete.countDown();

            await(abaComplete);
            final boolean updated = value.compareAndSet(expectedValue, STALE_UPDATE_VALUE, expectedStamp, expectedStamp + 1);
            log("Stale update from " + expectedValue + " to " + STALE_UPDATE_VALUE + " was " + (updated ? "accepted" : "rejected"));
        }, "stale-updater");

        final Thread abaUpdater = new Thread(() -> {
            await(staleReadComplete);
            if (!value.compareAndSet(INITIAL_VALUE, INTERMEDIATE_VALUE, INITIAL_STAMP, INITIAL_STAMP + 1)) {
                throw new IllegalStateException("Could not change A to B");
            }
            if (!value.compareAndSet(INTERMEDIATE_VALUE, INITIAL_VALUE, INITIAL_STAMP + 1, INITIAL_STAMP + 2)) {
                throw new IllegalStateException("Could not change B back to A");
            }
            log("ABA updater changed A to B and back to A; the current stamp is " + value.getStamp());
            abaComplete.countDown();
        }, "aba-updater");

        staleUpdater.start();
        abaUpdater.start();
        waitForAllThreadsToComplete(staleUpdater, abaUpdater);
        log("Done");
    }
}
