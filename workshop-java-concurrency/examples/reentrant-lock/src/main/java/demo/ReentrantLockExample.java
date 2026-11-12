package demo;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logSection;
import static java.util.Objects.requireNonNull;

public final class ReentrantLockExample {

    public static void run() {
        logSection("Deposit money using several threads");

        final BankAccount account = new BankAccount();
        log("Balance before all threads start: " + account.balance());

        /* Deposit money using several threads */
        CompletableFuture.allOf(
                        IntStream.range(0, 12)
                                .mapToObj(ignored -> BigDecimal.valueOf(100))
                                .map(adjustment -> (Runnable) () -> account.adjustBy(adjustment))
                                .map(CompletableFuture::runAsync)
                                .toArray(CompletableFuture[]::new))
                .join();

        log("Balance after all threads finish: " + account.balance());
    }

    static final class BankAccount {

        private final Lock lock = new ReentrantLock();
        /* Protected by the ReentrantLock lock */
        private BigDecimal balance = BigDecimal.ZERO;

        public void adjustBy(final BigDecimal adjustment) {
            requireNonNull(adjustment);

            lock.lock(); /* Acquire the lock to safely adjust the balance */
            try {
                log("The amount of " + balance + " is adjusted by: " + adjustment);
                balance = balance.add(adjustment);
                log("The balance after adjustment " + balance);
            } finally {
                lock.unlock(); /* Release the lock */
            }
        }

        public BigDecimal balance() {
            lock.lock(); /* Acquire the lock to safely read the balance */
            try {
                return balance;
            } finally {
                lock.unlock(); /* Release the lock */
            }
        }
    }
}
