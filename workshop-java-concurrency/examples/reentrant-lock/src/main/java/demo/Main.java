package demo;

import static demo.utils.Logs.logHeaderAndJavaVersion;
import static demo.utils.Logs.logSection;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("ReentrantLock");

        logSection("ReentrantLock example");
        ReentrantLockExample.run();

        logSection("Dead lock avoidance example");
        DeadLockAvoidanceExample.run();

        logSection("ReentrantLock methods example");
        ReentrantLockMethodsExamples.run();
    }
}
