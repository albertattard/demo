package demo;

import static demo.utils.Logs.log;
import static demo.utils.Threads.sleepForAFewSeconds;

public final class Task implements Runnable {

    @Override
    public void run() {
        log("Running task");
        sleepForAFewSeconds();
        log("Task completed");
    }
}
