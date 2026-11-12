package demo;

public final class CounterSync implements Counter {

    private int count;

    public CounterSync() {
        this(0);
    }

    public CounterSync(final int initialValue) {
        this.count = initialValue;
    }

    @Override
    public synchronized int increment() {
        return ++count;
    }

    @Override
    public synchronized int decrement() {
        return --count;
    }

    @Override
    public synchronized int value() {
        return count;
    }
}
