package demo;

import java.util.concurrent.locks.ReentrantLock;

import static demo.utils.Logs.log;

public final class ReentrantLockMethodsExamples {

    public static void run() {
        isHeldByCurrentThreadExample();
    }

    private static void isHeldByCurrentThreadExample() {
        final ReentrantLock lock = new ReentrantLock();

        log("Before acquiring the lock: " + lock.isHeldByCurrentThread());

        lock.lock();
        try {
            log("After acquiring the lock: " + lock.isHeldByCurrentThread());

            lock.lock();
            try {
                log("After re-entering the lock: " + lock.isHeldByCurrentThread()
                        + "; hold count: " + lock.getHoldCount());
            } finally {
                lock.unlock();
            }

            log("After releasing the nested acquisition: " + lock.isHeldByCurrentThread()
                    + "; hold count: " + lock.getHoldCount());
        } finally {
            lock.unlock();
        }

        log("After releasing the lock: " + lock.isHeldByCurrentThread());
    }
}
