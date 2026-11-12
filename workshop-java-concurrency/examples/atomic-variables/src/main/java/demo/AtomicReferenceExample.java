package demo;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static demo.utils.CountDownLatches.await;
import static demo.utils.Logs.log;
import static demo.utils.Threads.sleepForAFewMilliseconds;
import static java.util.Objects.requireNonNull;

public final class AtomicReferenceExample {

    private static final int NUMBER_OF_WORKERS = 8;

    public static void run() {
        /* An empty bank account with */
        final BankAccount account = new BankAccount();

        /* A thousand tasks try to deduct 1 from the bank account. */
        final int numberOfWithdrawals = 1_000;

        /* Add enough money so that at the end we will be left with 0 */
        account.adjustBy(BigDecimal.valueOf(numberOfWithdrawals));

        /* Releases the available workers only after all withdrawals have been submitted. */
        final CountDownLatch startGate = new CountDownLatch(1);

        /* Only in Java 19 the ExecutorService implements AutoClosable! */
        final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_WORKERS);
        try {
            /* Schedule all withdrawals without requiring one worker thread per withdrawal. */
            final CompletableFuture<?>[] withdrawals = IntStream.rangeClosed(1, numberOfWithdrawals)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        await(startGate);
                        account.adjustBy(BigDecimal.valueOf(-1));
                    }, executor))
                    .toArray(CompletableFuture[]::new);

            startGate.countDown();
            CompletableFuture.allOf(withdrawals).join();
        } finally {
            startGate.countDown();
            executor.shutdown();
        }

        log("The value of the account after " + numberOfWithdrawals + " withdrawals is " + account.balance());
        log("Done");
    }

    static final class BankAccount {

        private final AtomicReference<BigDecimal> balance = new AtomicReference<>(BigDecimal.ZERO);

        public void adjustBy(final BigDecimal adjustment) {
            requireNonNull(adjustment, "Cannot adjust balance by null");

            while (true) {
                final BigDecimal current = balance.get();
                final BigDecimal newValue = current.add(adjustment);
                if (balance.compareAndSet(current, newValue)) {
                    return;
                }

                sleepForAFewMilliseconds();
            }
        }

        public BigDecimal balance() {
            return balance.get();
        }
    }
}
