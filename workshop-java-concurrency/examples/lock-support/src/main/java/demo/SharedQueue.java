package demo;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

import static demo.utils.Logs.log;
import static demo.utils.Numbers.requireGreaterThanZero;

public final class SharedQueue {

    private final Queue<Integer> queue = new LinkedList<>();
    private final int capacity;

    /* Thread-safe queues for tracking blocked threads */
    private final Queue<Thread> waitingProducers = new ConcurrentLinkedQueue<>();
    private final Queue<Thread> waitingConsumers = new ConcurrentLinkedQueue<>();

    public SharedQueue(final int capacity) {
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
                    unparkNextWaitingConsumer();
                    return;
                }

                /* Register while holding the queue lock. This prevents a consumer from freeing space and missing this
                    producer between the full-queue check and registration. */
                waitingProducers.add(Thread.currentThread());
            }

            log("Queue is full. Producer is waiting...");
            LockSupport.park(); /* Wait until space is available */
            waitingProducers.remove(Thread.currentThread());
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
                    unparkNextWaitingProducer();
                    return value;
                }

                /* Register while holding the queue lock. This prevents a producer from adding an item and missing this
                    consumer between the empty-queue check and registration. */
                waitingConsumers.add(Thread.currentThread());
            }

            log("Queue is empty. Consumer is waiting...");
            LockSupport.park(); /* Wait until an item is available */
            waitingConsumers.remove(Thread.currentThread());
        }
    }

    private void unparkNextWaitingConsumer() {
        final Thread thread = waitingConsumers.poll();
        if (thread != null) {
            LockSupport.unpark(thread);
        }
    }

    private void unparkNextWaitingProducer() {
        final Thread thread = waitingProducers.poll();
        if (thread != null) {
            LockSupport.unpark(thread);
        }
    }

    public int capacity() {
        return capacity;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
