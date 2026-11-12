package demo;

import java.util.LinkedList;
import java.util.Queue;

import static demo.utils.Numbers.requirePositive;

public final class LinkedListIntQueue implements IntQueue {

    private final Queue<Integer> queue = new LinkedList<>();

    @Override
    public synchronized void add(final int number) {
        requirePositive(number);
        queue.add(number);
    }

    @Override
    public synchronized int poll() {
        final Integer value = queue.poll();
        return value == null ? NO_VALUE : value;
    }
}
