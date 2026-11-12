package demo.utils;

import java.util.concurrent.CountDownLatch;

public final class CountDownLatches {

    public static void await(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for the latch has counted down to zero", e);
        }
    }

    private CountDownLatches() {}
}
