package demo;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static demo.utils.CountDownLatches.await;
import static demo.utils.Logs.*;

public final class Main {

    private static final int READER_COUNT = 12;

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("StampedLock");

        logSection("Read the balance while one withdrawal occurs");

        final BankAccount account = new BankAccount(new BigDecimal(1_200));
        final CountDownLatch ready = new CountDownLatch(READER_COUNT + 1);
        final CountDownLatch start = new CountDownLatch(1);
        final ExecutorService executor = Executors.newFixedThreadPool(READER_COUNT + 1);

        try {
            final CompletableFuture<Void> withdrawal = CompletableFuture.runAsync(() -> {
                ready.countDown();
                await(start);
                account.withdraw(BigDecimal.valueOf(100));
            }, executor);
            final CompletableFuture<?>[] readers = IntStream.range(0, READER_COUNT)
                    .mapToObj(reader -> CompletableFuture.runAsync(() -> {
                        ready.countDown();
                        await(start);
                        log("Reader " + reader + " observed balance " + account.balance());
                    }, executor))
                    .toArray(CompletableFuture<?>[]::new);

            await(ready);
            start.countDown();

            CompletableFuture.allOf(readers).join();
            withdrawal.join();
        } finally {
            executor.shutdown();
        }

        log("Balance after the withdrawal: " + account.balance());
    }
}
