package demo;

import demo.utils.Logs;

import static demo.utils.Logs.*;
import static demo.utils.Threads.waitForAllThreadsToComplete;

public final class Main {

    public static void main(final String[] args) {
        Logs.logHeaderAndJavaVersion("Thread Builders");

        final Thread platformThread = Thread.ofPlatform()
                .name("Platform Thread")
                .uncaughtExceptionHandler((thread, exception) -> logError("The exception escaped platform thread: " + thread + "!!", exception))
                .start(Main::throwExpectedException);

        final Thread virtualThread = Thread.ofVirtual()
                .name("Virtual Thread")
                .uncaughtExceptionHandler((thread, exception) -> logError("The exception escaped virtual thread: " + thread + "!!", exception))
                .start(Main::throwExpectedException);

        /* Virtual threads are always daemon threads, so they do not keep the
            JVM alive. There is no public API to make a virtual thread
            non-daemon. If it must complete before the JVM exits, wait for it
            explicitly, for example by calling join(). */
        waitForAllThreadsToComplete(platformThread, virtualThread);

        logSection("ThreadFactory example");
        ThreadFactoryExample.run();

        log("Done");
    }

    private static void throwExpectedException() {
        throw new IllegalStateException("Expected exception");
    }
}
