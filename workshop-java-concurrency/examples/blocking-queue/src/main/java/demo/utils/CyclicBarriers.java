package demo.utils;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public final class CyclicBarriers {

    public static void await(final CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for the barrier has counted down to zero", e);
        } catch (final BrokenBarrierException e) {
            throw new RuntimeException("Broken cyclic barrier", e);
        }
    }

    private CyclicBarriers() {}
}
