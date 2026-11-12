package demo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import static demo.utils.Logs.log;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.util.Objects.requireNonNull;

public final class DelayQueueExample {

    public static void run() {
        final int numberOfElements = 3;
        final BlockingQueue<Task> queue = new DelayQueue<>();

        log("Pushing elements to the queue in no particular priority/order");
        putElementToQueue(Task.delayedBy(Duration.ofSeconds(5), 1), queue);
        putElementToQueue(Task.delayedBy(Duration.ofSeconds(1), 2), queue);
        putElementToQueue(Task.delayedBy(Duration.ofSeconds(3), 3), queue);
        log("Producer completed");

        log("Fetching elements from the queue based on availability (and not order of insertion)");
        for (int i = 0; i < numberOfElements; i++) {
            log("Fetching the next element from the queue");
            final Task element = takeNextElementFromQueue(queue);
            log("Fetched element: " + element + " from the queue");
        }

        log("After the consumer and producer have completed, the queue contains: " + queue);
        log("Done");
    }

    private static Task takeNextElementFromQueue(final BlockingQueue<Task> queue) {
        requireNonNull(queue, "Cannot take elements from a null queue");

        try {
            return queue.take(); /* Blocks until an element with an expired delay is available */
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for the next element to become available", e);
        }
    }

    private static void putElementToQueue(final Task element, final BlockingQueue<Task> queue) {
        requireNonNull(element, "Cannot put null elements to the queue");
        requireNonNull(queue, "Cannot put elements to a null queue");

        try {
            log("Putting element " + element);
            queue.put(element); /* Never blocks: DelayQueue is unbounded */
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for more space to be available", e);
        }
    }

    static final class Task implements Delayed {

        private final LocalDateTime startTime;
        private final int number;

        static Task delayedBy(final Duration delay, final int number) {
            return new Task(LocalDateTime.now().plus(delay), number);
        }

        private Task(final LocalDateTime startTime, final int number) {
            this.startTime = startTime;
            this.number = number;
        }

        @Override
        public long getDelay(final TimeUnit unit) {
            return unit.convert(Duration.between(LocalDateTime.now(), startTime).toMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(final Delayed other) {
            return startTime.compareTo(((Task) other).startTime);
        }

        @Override
        public String toString() {
            return "Task[startTime=" + ISO_LOCAL_TIME.format(startTime) + ", number=" + number + ']';
        }
    }
}
