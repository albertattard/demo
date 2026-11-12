package demo;

import static demo.utils.Logs.logHeaderAndJavaVersion;
import static demo.utils.Logs.logSection;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("Concurrent Linked Deque");

        logSection("Deque example");
        DequeExample.run();

        logSection("Work stealing example");
        WorkStealingExample.run();
    }
}
