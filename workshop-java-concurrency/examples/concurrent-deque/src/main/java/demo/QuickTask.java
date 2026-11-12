package demo;

import static demo.utils.Logs.log;
import static demo.utils.Threads.sleepForAFewMilliseconds;

public final class QuickTask implements Runnable {

    private final int number;

    public QuickTask(final int number) {
        this.number = number;
    }

    @Override
    public void run() {
        log("Starting quick task " + number);
        sleepForAFewMilliseconds();
        log("Quick task " + number + " completed");
    }

    @Override
    public String toString() {
        return "QuickTask[number=" + number + ']';
    }
}
