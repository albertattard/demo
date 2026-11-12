package demo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logHeaderAndJavaVersion;
import static demo.utils.Threads.sleepForAFewSeconds;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("Exchanger");

        final Exchanger<String> exchanger = new Exchanger<>();

        /* Only in Java 19 the ExecutorService implements AutoClosable! */
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            final CompletableFuture<Void> alice = CompletableFuture.runAsync(() -> {
                final String message = "Hello from Alice";
                log("Alice is sending the message: '" + message + '\'');

                try {
                    final String received = exchanger.exchange(message);
                    log("Alice received the message: '" + received + '\'');
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for a reply", e);
                }
            }, executor);

            final CompletableFuture<Void> bob = CompletableFuture.runAsync(() -> {
                log("Bob is preparing the message...");
                sleepForAFewSeconds();

                final String message = "Hello from Bob";
                log("Bob is sending the message: '" + message + '\'');

                try {
                    final String received = exchanger.exchange(message);
                    log("Bob received the message: '" + received + '\'');
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for a reply", e);
                }
            }, executor);

            /* Wait for both the consumer and producer to complete */
            CompletableFuture.allOf(alice, bob).join();
        } finally {
            executor.shutdown();
        }

        log("Done");
    }
}
