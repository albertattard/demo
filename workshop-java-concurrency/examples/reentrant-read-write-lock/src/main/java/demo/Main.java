package demo;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static demo.utils.Logs.*;
import static demo.utils.Numbers.isNegative;
import static demo.utils.Numbers.requirePositive;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("ReentrantReadWriteLock");

        logSection("Read balances while occasional withdrawals occur");

        final BankAccount account = new BankAccount(new BigDecimal(1_200L));
        log("Balance before all threads start: " + account.balance());

        /* Most operations are reads; only two change the balance. */
        final Runnable read = () -> log("Observed balance: " + account.balance());
        final Runnable write = () -> account.withdraw(BigDecimal.valueOf(100));
        CompletableFuture.allOf(Stream.concat(
                                IntStream.range(0, 12).mapToObj(i -> read),
                                IntStream.range(0, 2).mapToObj(i -> write))
                        .map(CompletableFuture::runAsync)
                        .toArray(CompletableFuture[]::new))
                .join();

        log("Balance after all threads finish: " + account.balance());
    }

    static final class BankAccount {

        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        /* Protected by the ReentrantReadWriteLock lock */
        private BigDecimal balance;

        BankAccount(final BigDecimal initialBalance) {
            this.balance = requirePositive(initialBalance);
        }

        public void withdraw(final BigDecimal amount) {
            requirePositive(amount);

            lock.readLock().lock(); /* Acquire the lock to safely read the balance */
            try {
                log("Read log acquired");
                final BigDecimal newBalance = balance.add(amount.negate());
                if (isNegative(newBalance)) {
                    log("There is not enough money (" + balance + ") to cover the withdrawal (" + amount + ')');
                    return;
                }
            } finally {
                log("Releasing the read lock");
                lock.readLock().unlock();
            }

            /* ReentrantReadWriteLock does not support upgrading from the read lock to the write lock: a thread that
                holds a read lock and then requests the write lock waits indefinitely. Release the read lock first,
                but revalidate the state after acquiring the write lock because another thread may change it. A thread
                that holds the write lock can acquire the read lock and then release the write lock to downgrade. */
            lock.writeLock().lock();
            try {
                log("Write log acquired");
                /* Another thread may have updated the value once the read lock was released */
                final BigDecimal newBalanceBeforeWrite = balance.add(amount.negate());
                if (isNegative(newBalanceBeforeWrite)) {
                    log("There is not enough money (" + balance + ") to cover the withdrawal (" + amount + ')');
                    return;
                }

                log("Withdrawing " + amount + " from " + balance);
                balance = newBalanceBeforeWrite;
                log("New balance after withdrawal " + balance);
            } finally {
                log("Releasing the write lock");
                lock.writeLock().unlock();
            }
        }

        public BigDecimal balance() {
            lock.readLock().lock(); /* Acquire the lock to safely read the balance */
            try {
                return balance;
            } finally {
                lock.readLock().unlock(); /* Release the lock */
            }
        }
    }
}
