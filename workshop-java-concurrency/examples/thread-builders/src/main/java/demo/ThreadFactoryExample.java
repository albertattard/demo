package demo;

import java.util.concurrent.ThreadFactory;

import static demo.utils.Logs.log;
import static demo.utils.Threads.sleepForAFewMilliseconds;
import static demo.utils.Threads.waitForAllThreadsToComplete;

public final class ThreadFactoryExample {

    public static void run() {
        final ThreadFactory workers = Thread.ofVirtual()
                .name("Virtual worker-", 0)
                .factory();

        final Thread firstWorker = workers.newThread(ThreadFactoryExample::runTask);
        final Thread secondWorker = workers.newThread(ThreadFactoryExample::runTask);
        firstWorker.start();
        secondWorker.start();

        waitForAllThreadsToComplete(firstWorker, secondWorker);
    }

    private static void runTask() {
        log("Running...");
        sleepForAFewMilliseconds();
        log("Done");
    }

    private ThreadFactoryExample() {}
}
