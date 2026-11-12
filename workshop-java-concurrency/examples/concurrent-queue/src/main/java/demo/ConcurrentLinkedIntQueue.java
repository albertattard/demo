package demo;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static demo.utils.Numbers.requirePositive;

public final class ConcurrentLinkedIntQueue implements IntQueue {

    private final Queue<Integer> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void add(final int number) {
        requirePositive(number);
        queue.add(number);
    }

    @Override
    public int poll() {
        final Integer value = queue.poll();
        return value == null ? NO_VALUE : value;
    }
}
