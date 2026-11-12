package demo;

import java.util.concurrent.atomic.AtomicInteger;

public final class CounterAtomic implements Counter {

    private final AtomicInteger count;

    public CounterAtomic() {
        this(0);
    }

    public CounterAtomic(final int initialValue) {
        this.count = new AtomicInteger(initialValue);
    }

    @Override
    public int increment() {
        return count.incrementAndGet();
    }

    @Override
    public int decrement() {
        return count.decrementAndGet();
    }

    @Override
    public int value() {
        return count.get();
    }
}
