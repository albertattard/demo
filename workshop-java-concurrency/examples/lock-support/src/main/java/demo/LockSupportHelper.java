package demo;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

public final class LockSupportHelper {

    private final Queue<Thread> waiting = new ConcurrentLinkedQueue<>();

    public void registerCurrentThread() {
        waiting.add(Thread.currentThread());
    }

    public void parkCurrentThread() {
        LockSupport.park();
        waiting.remove(Thread.currentThread());
    }

    public void unpark() {
        final Thread thread = waiting.poll();
        if (thread != null) {
            LockSupport.unpark(thread);
        }
    }
}
