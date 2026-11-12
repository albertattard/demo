package demo;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class CounterLock implements Counter {

    private final Lock lock = new ReentrantLock();
    private int count;

    public CounterLock() {
        this(0);
    }

    public CounterLock(final int initialValue) {
        this.count = initialValue;
    }

    @Override
    public int increment() {
        lock.lock();
        try {
            return ++count;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int decrement() {
        lock.lock();
        try {
            return --count;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int value() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
}
