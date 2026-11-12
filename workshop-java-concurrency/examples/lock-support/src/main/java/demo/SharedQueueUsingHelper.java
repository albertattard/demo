package demo;

import java.util.LinkedList;
import java.util.Queue;

import static demo.utils.Logs.log;
import static demo.utils.Numbers.requireGreaterThanZero;

public final class SharedQueueUsingHelper {

    private final Queue<Integer> queue = new LinkedList<>();
    private final int capacity;

    /* Thread-safe queues for tracking blocked threads */
    private final LockSupportHelper waitingProducers = new LockSupportHelper();
    private final LockSupportHelper waitingConsumers = new LockSupportHelper();

    public SharedQueueUsingHelper(final int capacity) {
        this.capacity = requireGreaterThanZero(capacity);
    }

    public void produce(final int value) {
        while (true) {
            synchronized (this) {
                /* These two operations (`queue.size() < capacity` and `queue.offer(value)`) need to be done
                    atomically, otherwise there may be a race condition and two or more threads may be able to add
                    elements together, exceeding the queue capacity. */
                if (queue.size() < capacity) {
                    queue.offer(value);
                    log("Produced: " + value + " (size: " + queue.size() + ')');
                    waitingConsumers.unpark();
                    return;
                }

                /* Register while holding the queue lock, then park after releasing it. */
                waitingProducers.registerCurrentThread();
            }

            log("Queue is full. Producer is waiting...");
            waitingProducers.parkCurrentThread(); /* Wait until space is available */
        }
    }

    public int consume() {
        while (true) {
            synchronized (this) {
                /* These two operations (`queue.isEmpty()` and `queue.poll()`) need to be done atomically, otherwise
                    there may be a race condition and two or more threads may be able to poll elements at the same
                    time, even when there are no more elements available.*/
                if (!queue.isEmpty()) {
                    final int value = queue.poll();
                    log("Consumed: " + value + " (size: " + queue.size() + ')');
                    waitingProducers.unpark();
                    return value;
                }

                /* Register while holding the queue lock, then park after releasing it. */
                waitingConsumers.registerCurrentThread();
            }

            log("Queue is empty. Consumer is waiting...");
            waitingConsumers.parkCurrentThread(); /* Wait until an item is available */
        }
    }

    public int capacity() {
        return capacity;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
