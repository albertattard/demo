package demo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.IntStream;

import static demo.PriorityBlockingQueueExample.TaskPriority.POISON_PILL;
import static demo.utils.Logs.log;
import static demo.utils.Threads.sleepForAFewMilliseconds;
import static java.util.Objects.requireNonNull;

public final class PriorityBlockingQueueExample {

    public static void run() {
        final BlockingQueue<Task> queue = new PriorityBlockingQueue<>();

        log("Pushing the 'poison pill' element to the queue as the first element.");
        log("The 'poison pill' element has the lowest priority, so it will always be processed last.");
        log("Once the consumer reads this element it stops processing the queue.");
        putElementToQueue(Task.poisonPill(), queue);

        log("Pushing elements to the queue in no particular priority/order");
        IntStream.rangeClosed(1, 10)
                .mapToObj(Task::next)
                .peek(element -> log("Putting element " + element))
                .forEach(element -> putElementToQueue(element, queue));
        log("Producer completed");

        log("Fetching elements from the queue based on their priority (and not order of insertion)");
        while (true) {
            log("Fetching the next element from the queue");
            final Task element = takeNextElementFromQueue(queue);
            if (element.isPoisonPill()) {
                log("No more elements to process");
                log("Consumer completed");
                break;
            }

            log("Fetched element: " + element + " from the queue");
            element.run();
        }

        log("After the consumer and producer have completed, the queue contains: " + queue);
        log("Done");
    }

    private static Task takeNextElementFromQueue(final BlockingQueue<Task> queue) {
        requireNonNull(queue, "Cannot take elements from a null queue");

        try {
            return queue.take(); /* Blocks if the queue is empty */
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for the next element to become available", e);
        }
    }

    private static void putElementToQueue(final Task element, final BlockingQueue<Task> queue) {
        requireNonNull(element, "Cannot put null elements to the queue");
        requireNonNull(queue, "Cannot put elements to a null queue");

        try {
            queue.put(element); /* Blocks if the queue is full */
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for more space to be available", e);
        }
    }

    static final class Task implements Runnable, Comparable<Task> {

        private final TaskPriority priority;
        private final int number;

        static Task next(final int number) {
            return number % 8 == 0 ? Task.urgent(number)
                    : number % 4 == 0 ? Task.high(number)
                    : number % 2 == 0 ? Task.normal(number)
                    : Task.low(number);
        }

        static Task urgent(final int number) {
            return new Task(TaskPriority.URGENT, number);
        }

        static Task high(final int number) {
            return new Task(TaskPriority.HIGH, number);
        }

        static Task normal(final int number) {
            return new Task(TaskPriority.NORMAL, number);
        }

        static Task low(final int number) {
            return new Task(TaskPriority.LOW, number);
        }

        static Task poisonPill() {
            return new Task(POISON_PILL, -1);
        }

        private Task(final TaskPriority priority, final int number) {
            this.priority = requireNonNull(priority);
            this.number = number;
        }

        @Override
        public void run() {
            log("Running task with number " + number + " and " + priority.name().toLowerCase() + " priority");
            sleepForAFewMilliseconds();
        }

        public boolean isPoisonPill() {
            return priority == POISON_PILL;
        }

        @Override
        public int compareTo(final Task other) {
            return priority.compareTo(other.priority);
        }

        @Override
        public String toString() {
            return isPoisonPill()
                    ? "Poison Pill"
                    : "Task[priority=" + priority + ", number=" + number + ']';
        }
    }

    enum TaskPriority {
        URGENT,
        HIGH,
        NORMAL,
        LOW,
        POISON_PILL /* Make sure that the poison pill element is always at the end of the queue! */;
    }
}
