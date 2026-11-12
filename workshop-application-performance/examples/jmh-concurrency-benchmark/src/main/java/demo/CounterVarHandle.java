package demo;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public final class CounterVarHandle implements Counter {

    private volatile int value;
    private static final VarHandle VALUE;

    static {
        try {
            VALUE = MethodHandles.lookup()
                    .in(CounterVarHandle.class)
                    .findVarHandle(CounterVarHandle.class, "value", int.class);
        } catch (final ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public CounterVarHandle() {
        this(0);
    }

    public CounterVarHandle(final int initialValue) {
        this.value = initialValue;
    }

    @Override
    public int increment() {
        return ((int) VALUE.getAndAdd(this, 1)) + 1;
    }

    @Override
    public int decrement() {
        return ((int) VALUE.getAndAdd(this, -1)) - 1;
    }

    @Override
    public int value() {
        return (int) VALUE.get(this);
    }
}
