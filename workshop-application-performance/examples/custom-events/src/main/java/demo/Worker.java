package demo;

import module java.base;

public final class Worker implements Runnable {

    @Override
    public void run() {
        final RandomGenerator generator = RandomGenerator.getDefault();

        while (true) {
            final WorkEvent event = new WorkEvent();
            try {
                event.begin();

                /* Do Important things */
                final long transactionId = generator.nextLong();
                sleepForTwoHundredMilliseconds();

                /* Set custom values */
                event.transactionId(transactionId);
            } finally {
                event.end();
                event.commit();
            }

            Thread.yield();
        }
    }

    private static void sleepForTwoHundredMilliseconds() {
        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while sleeping", e);
        }
    }
}
