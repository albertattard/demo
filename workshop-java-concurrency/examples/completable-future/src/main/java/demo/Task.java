package demo;

import java.util.function.Supplier;

import static demo.utils.Logs.log;
import static demo.utils.Threads.sleepForAFewSeconds;
import static java.util.Objects.requireNonNull;

public final class Task implements Runnable, Supplier<String> {

    private final String name;
    private final boolean fail;

    static Runnable runnable(final String name) {
        requireNonNull(name);
        return new Task(name, false);
    }

    static Supplier<String> supplier(final String name) {
        requireNonNull(name);
        return new Task(name, false);
    }

    static Supplier<String> failingSupplier(final String name) {
        requireNonNull(name);
        return new Task(name, true);
    }

    private Task(final String name, final boolean fail) {
        this.name = name;
        this.fail = fail;
    }

    @Override
    public void run() {
        log(name + " started");
        sleepForAFewSeconds();
        if (fail) {
            throw new RuntimeException("Simulating errors");
        }
        log(name + " complete");
    }

    @Override
    public String get() {
        run();
        return name;
    }
}
