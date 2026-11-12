package demo;

import jdk.jfr.Recording;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static final int TASKS = 2_000;
    private static final int CONCURRENCY = 8;
    private static final AtomicInteger completed = new AtomicInteger();
    private static final AtomicInteger names = new AtomicInteger();

    static void main(final String[] args) throws Exception {
        if (args.length != 2 || !(args[0].equals("churn") || args[0].equals("pool"))) {
            throw new IllegalArgumentException("Usage: java -jar thread-churn-1.0.0.jar churn|pool recording.jfr");
        }

        try (Recording recording = new Recording()) {
            recording.enable("jdk.ThreadStart").withStackTrace();
            recording.enable("jdk.ThreadEnd").withStackTrace();
            recording.start();

            long start = System.nanoTime();

            switch (args[0]) {
                case "churn" -> churn();
                case "pool" -> pool();
            }

            long elapsed = System.nanoTime() - start;
            recording.stop();
            recording.dump(Path.of(args[1]));

            System.out.printf(Locale.ROOT, "mode=%s tasks=%d completed=%d concurrency=%d elapsed=%.1f ms throughput=%.1f tasks/s%n",
                    args[0], TASKS, completed.get(), CONCURRENCY, elapsed / 1_000_000.0,
                    TASKS * 1_000_000_000.0 / elapsed);
        }
    }

    private static void churn() throws InterruptedException {
        for (int first = 0; first < TASKS; first += CONCURRENCY) {
            final List<Thread> batch = new ArrayList<>();

            for (int i = first; i < Math.min(first + CONCURRENCY, TASKS); i++) {
                final Thread thread = builder().start(Main::task);
                batch.add(thread);
            }

            for (final Thread thread : batch) {
                thread.join();
            }
        }
    }

    private static void pool() throws InterruptedException, ExecutionException {
        final ThreadFactory threadFactory = runnable -> builder().unstarted(runnable);
        try (ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY, threadFactory)) {
            for (int first = 0; first < TASKS; first += CONCURRENCY) {
                final List<Future<?>> batch = new ArrayList<>();

                for (int i = first; i < Math.min(first + CONCURRENCY, TASKS); i++) {
                    batch.add(executor.submit(Main::task));
                }

                for (final Future<?> future : batch) {
                    future.get();
                }
            }
        }
    }

    private static Thread.Builder.OfPlatform builder() {
        /* Prefix the thread names with 'work-' to make it easier to distinguish these threads from the rest */
        return Thread.ofPlatform().name("work-" + names.incrementAndGet());
    }

    private static void task() {
        // The same short, interruptible wait models a small blocking operation.
        try {
            TimeUnit.MILLISECONDS.sleep(1);
            completed.incrementAndGet();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
