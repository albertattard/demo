package demo;

import module java.base;
import module jdk.management;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logHeaderAndJavaVersion;

public final class Main {

    private static final Duration WAIT_LIMIT = Duration.ofSeconds(2);
    private static final Duration WORK_LIMIT = Duration.ofSeconds(10);

    static void main() throws InterruptedException {
        logHeaderAndJavaVersion("Virtual Thread scheduler");

        final VirtualThreadSchedulerMXBean scheduler = scheduler();
        final int originalParallelism = scheduler.getParallelism();
        final Thread worker = Thread.ofVirtual().name("CARRIER-OCCUPIER").unstarted(Main::occupyCarrier);
        final Thread probe = Thread.ofVirtual().name("QUEUED-PROBE").unstarted(() -> {});

        try {
            scheduler.setParallelism(1);
            printScheduler("Target set to 1", scheduler);
            if (Thread.interrupted()) {
                throw new InterruptedException("Controller interrupted before starting work");
            }
            worker.start();
            awaitMountedWorker(scheduler, worker);
            probe.start();

            final boolean completedBefore = probe.join(Duration.ofMillis(200));
            printScheduler("Before adjustment", scheduler);
            log("Probe completed before adjustment: " + completedBefore);

            scheduler.setParallelism(2);
            final boolean completedAfter = probe.join(WAIT_LIMIT);
            final boolean workerStillRunning = worker.isAlive();
            printScheduler("After adjustment", scheduler);
            log("Probe completed after adjustment: " + completedAfter);
            log("Occupier still running at observation: " + workerStillRunning);
            if (completedBefore || !completedAfter || !workerStillRunning) {
                log("Starvation/recovery sequence was not observed in this run.");
            }
        } finally {
            worker.interrupt();
            try {
                awaitTermination(worker, probe);
            } finally {
                scheduler.setParallelism(originalParallelism);
                printScheduler("Original target restored", scheduler);
            }
        }
        log("Both virtual threads terminated.");
    }

    private static void occupyCarrier() {
        final long started = System.nanoTime();
        // Deliberately perform no blocking operation: keep this carrier occupied.
        while (!Thread.currentThread().isInterrupted()
                && System.nanoTime() - started < WORK_LIMIT.toNanos()) {
            Thread.onSpinWait();
        }
    }

    private static void awaitMountedWorker(final VirtualThreadSchedulerMXBean scheduler, final Thread worker) {
        final long started = System.nanoTime();
        while (scheduler.getMountedVirtualThreadCount() < 1 && worker.isAlive()) {
            if (System.nanoTime() - started >= WAIT_LIMIT.toNanos()) {
                throw new IllegalStateException("No mounted worker observed within " + WAIT_LIMIT);
            }
            Thread.onSpinWait();
        }
    }

    private static void printScheduler(final String label, final VirtualThreadSchedulerMXBean scheduler) {
        log(label + ": target=" + scheduler.getParallelism()
                + ", pool=" + scheduler.getPoolSize()
                + ", mounted~=" + scheduler.getMountedVirtualThreadCount()
                + ", queued~=" + scheduler.getQueuedVirtualThreadCount());
    }

    private static void awaitTermination(final Thread... threads) {
        final long started = System.nanoTime();
        boolean interrupted = false;
        try {
            for (final Thread thread : threads) {
                while (thread.isAlive()) {
                    final long remaining = WAIT_LIMIT.toNanos() - (System.nanoTime() - started);
                    if (remaining <= 0) {
                        throw new IllegalStateException("Thread did not terminate: " + thread.getName());
                    }
                    try {
                        thread.join(Duration.ofNanos(remaining));
                    } catch (final InterruptedException e) {
                        interrupted = true;
                    }
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static VirtualThreadSchedulerMXBean scheduler() {
        return ManagementFactory.getPlatformMXBean(VirtualThreadSchedulerMXBean.class);
    }

    private Main() {}
}
