package demo;

import java.time.Duration;

final class PinnedVirtualThread {

    private static final Duration BLOCKING_TIME = Duration.ofMillis(100);

    static void run() throws InterruptedException {
        final Object monitor = new Object();
        final Thread thread = Thread.ofVirtual()
                .name("pinned-blocking-task")
                .start(() -> blockWhileHolding(monitor));
        thread.join();
    }

    private static void blockWhileHolding(final Object monitor) {
        synchronized (monitor) {
            try {
                Thread.sleep(BLOCKING_TIME);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Pinned task was interrupted", e);
            }
        }
    }

    private PinnedVirtualThread() {}
}
