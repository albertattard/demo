package demo;

import demo.utils.Threads;

import java.time.Duration;
import java.util.List;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logHeaderAndJavaVersion;
import static demo.utils.Threads.waitForAllThreadsToComplete;

public final class Main {

    public static void main(final String[] args) throws InterruptedException {
        logHeaderAndJavaVersion("Virtual Threads");

        final List<Thread> requests = List.of(
                startBlockingRequest("request-1"),
                startBlockingRequest("request-2"),
                startBlockingRequest("request-3"));

        log("Started " + requests.size() + " virtual threads for independent blocking requests.");
        waitForAllThreadsToComplete(requests);
        log("All requests completed.");

        PinnedVirtualThread.run();
        log("Pinned-task diagnostic completed.");
    }

    private static Thread startBlockingRequest(final String name) {
        return Thread.ofVirtual()
                .name(name)
                .start(() -> {
                    log("The request runs as a virtual thread: " + Thread.currentThread().isVirtual());
                    Threads.sleepForAFewMilliseconds();
                });
    }

    private Main() {}
}
