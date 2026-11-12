package demo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static demo.utils.Logs.log;

public final class ThreadLocalExample {

    private static final ThreadLocal<String> REQUEST_MESSAGE = ThreadLocal.withInitial(() -> "NO REQUEST MESSAGE");

    public static void run() {
        log("Without ThreadLocal, every method in the call chain needs the request context");
        log("Alice received the message: '" + passThroughVariableWhichIsNeededByAnotherMethodMethodA("Message from Alice") + '\'');
        log("Bob received the message: '" + passThroughVariableWhichIsNeededByAnotherMethodMethodA("Message from Bob") + '\'');

        /* Only in Java 19 the ExecutorService implements AutoClosable! */
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            final CompletableFuture<Void> alice = CompletableFuture.runAsync(
                    () -> logReceivedMessage("Alice", "Message from Alice"), executor);

            final CompletableFuture<Void> bob = CompletableFuture.runAsync(
                    () -> logReceivedMessage("Bob", "Message from Bob"), executor);

            final CompletableFuture<Void> noRequest = CompletableFuture.allOf(alice, bob)
                    .thenRunAsync(() -> log("Later task received the message: '"
                            + callMethodThatReadFromThreadLocalMethodA() + '\''), executor);

            /* Wait for every task to complete */
            CompletableFuture.allOf(alice, bob, noRequest).join();
        } finally {
            executor.shutdown();
        }

        log("Done");
    }

    private static void logReceivedMessage(final String name, final String message) {
        REQUEST_MESSAGE.set(message);
        try {
            final String received = callMethodThatReadFromThreadLocalMethodA();
            log(name + " received the message: '" + received + '\'');
        } finally {
            REQUEST_MESSAGE.remove();
        }
    }

    private static String passThroughVariableWhichIsNeededByAnotherMethodMethodA(final String message) {
        return passThroughVariableWhichIsNeededByAnotherMethodMethodB(message);
    }

    private static String passThroughVariableWhichIsNeededByAnotherMethodMethodB(final String message) {
        return requiresTheVariableMethodC(message);
    }

    private static String requiresTheVariableMethodC(final String message) {
        return message;
    }

    private static String callMethodThatReadFromThreadLocalMethodA() {
        return callMethodThatReadFromThreadLocalMethodB();
    }

    private static String callMethodThatReadFromThreadLocalMethodB() {
        return readsFromThreadLocalMethodC();
    }

    private static String readsFromThreadLocalMethodC() {
        /* Get the message from the ThreadLocal */
        return REQUEST_MESSAGE.get();
    }
}
