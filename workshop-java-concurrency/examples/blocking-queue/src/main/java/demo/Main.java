package demo;

import static demo.utils.Logs.logHeaderAndJavaVersion;
import static demo.utils.Logs.logSection;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("Blocking Queue");

        logSection("ArrayBlockingQueue example");
        ArrayBlockingQueueExample.run();

        logSection("LinkedBlockingQueue example");
        LinkedBlockingQueueExample.run();

        logSection("SynchronousQueue example");
        SynchronousQueueExample.run();

        logSection("PriorityBlockingQueue example");
        PriorityBlockingQueueExample.run();

        logSection("DelayQueue example");
        DelayQueueExample.run();
    }
}
