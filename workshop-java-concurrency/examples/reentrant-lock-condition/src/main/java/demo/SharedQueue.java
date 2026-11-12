package demo;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static demo.utils.Logs.log;
import static demo.utils.Numbers.requireGreaterThanZero;

public final class SharedQueue {

    private final Lock lock = new ReentrantLock();
    /* Condition to check if queue is not full */
    private final Condition notFull = lock.newCondition();
    /* Condition to check if queue is not empty */
    private final Condition notEmpty = lock.newCondition();

    /* Protected by the ReentrantLock lock */
    private final Queue<Integer> queue = new LinkedList<>();
    private final int capacity;

    public SharedQueue(final int capacity) {
        this.capacity = requireGreaterThanZero(capacity, "The capacity must be greater than zero");
    }

    public void produce(final int value) {
        lock.lock();
        try {
            while (queue.size() >= capacity) {
                log("Queue is full. Producer is waiting...");
                notFull.await();  /* Wait until space is available */
            }

            queue.offer(value);
            log("Produced: " + value + " (size: " + queue.size() + ')');
            notEmpty.signal(); /* Notify consumer that an item is available */
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting");
        } finally {
            lock.unlock();
        }
    }

    public int consume() {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                log("Queue is empty. Consumer is waiting...");
                notEmpty.await();  /* Wait until an item is available */
            }

            final int value = queue.poll();
            log("Consumed: " + value + " (size: " + queue.size() + ')');
            notFull.signal(); /* Notify producer that space is available */
            return value;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting");
        } finally {
            lock.unlock();
        }
    }

    public int capacity() {
        return capacity;
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return queue.isEmpty();
        } finally {
            lock.unlock();
        }
    }
}
