package demo;

import static demo.utils.Logs.log;
import static demo.utils.Threads.sleepForAFewSeconds;

public final class SlowTask implements Runnable {

    private final int number;

    public SlowTask(final int number) {
        this.number = number;
    }

    @Override
    public void run() {
        log("Starting slow task " + number);
        sleepForAFewSeconds();
        log("Slow task " + number + " completed");
    }

    @Override
    public String toString() {
        return "SlowTask[number=" + number + ']';
    }
}
