package demo;

import java.util.LinkedList;
import java.util.Queue;

import static demo.utils.Logs.log;
import static demo.utils.Numbers.requireGreaterThanZero;

public final class SharedQueue implements AutoCloseable {

    /* Protected by the synchronized(this) lock */
    private final Queue<Integer> queue = new LinkedList<>();
    private final int capacity;
    private boolean closed;

    public SharedQueue(final int capacity) {
        this.capacity = requireGreaterThanZero(capacity);
    }

    public synchronized void produce(final int value) {
        while (!closed && queue.size() >= capacity) {
            try {
                log("Queue is full. Producer is waiting...");
                wait(); /* Wait until space is available */
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                close();
                throw new RuntimeException("Interrupted while waiting", e);
            }
        }

        ensureOpen();
        queue.offer(value);
        log("Produced: " + value + " (size: " + queue.size() + ')');
        notifyAll(); /* Wake all waiters; each re-checks its condition */
    }

    public synchronized int consume() {
        while (!closed && queue.isEmpty()) {
            try {
                log("Queue is empty. Consumer is waiting...");
                wait(); /* Wait until an item is available */
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                close();
                throw new RuntimeException("Interrupted while waiting", e);
            }
        }

        if (queue.isEmpty()) {
            throw new IllegalStateException("The queue is closed");
        }

        final int value = queue.poll();
        log("Consumed: " + value + " (size: " + queue.size() + ')');
        notifyAll(); /* Wake all waiters; each re-checks its condition */
        return value;
    }

    @Override
    public synchronized void close() {
        closed = true;
        notifyAll(); /* Wake waiters so they can observe the closed state */
    }

    public int capacity() {
        return capacity;
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("The queue is closed");
        }
    }
}
