package demo;

import static demo.utils.Logs.log;

public final class Main {

    public static void main(final String[] args) throws InterruptedException {
        final Publication publication = new Publication();
        final Thread reader = new Thread(() -> {
            log("Fetching the publication message");
            log("Reader observed: " + publication.awaitMessage());
        }, "READER");

        log("Starting the reader thread");
        reader.start();

        log("Giving the reader time to enter awaitMessage()");
        // This is only a best-effort reproduction aid; sleep has no synchronisation semantics.
        Thread.sleep(500);

        log("Publishing the message");
        publication.publish("published before ready");

        log("Waiting for the reader to complete");
        reader.join();

        log("Done");
    }

    private Main() {}
}
