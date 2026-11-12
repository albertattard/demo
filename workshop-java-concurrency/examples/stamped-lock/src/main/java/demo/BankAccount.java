package demo;

import java.math.BigDecimal;
import java.util.concurrent.locks.StampedLock;

import static demo.utils.Logs.log;
import static demo.utils.Numbers.isNegative;
import static demo.utils.Numbers.requirePositive;

public final class BankAccount {

    private final StampedLock lock = new StampedLock();
    /* Protected by the StampedLock lock */
    private BigDecimal balance;

    BankAccount(final BigDecimal initialBalance) {
        this.balance = requirePositive(initialBalance);
    }

    public void withdraw(final BigDecimal amount) {
        requirePositive(amount);

        log("Acquiring optimistic read lock");
        long readStamp = lock.tryOptimisticRead(); /* Acquire optimistic read lock */
        BigDecimal observedBalance = balance;
        boolean readLockAcquired = false;
        try {
            if (lock.validate(readStamp)) { /* Validate the stamp */
                log("Acquired optimistic read lock");
            } else {
                log("Fallback to read lock.");
                readStamp = lock.readLock();
                readLockAcquired = true;
                observedBalance = balance;
            }

            final BigDecimal newBalance = observedBalance.add(amount.negate());
            if (isNegative(newBalance)) {
                log("There is not enough money (" + observedBalance + ") to cover the withdrawal (" + amount + ')');
                return;
            }

            long writeStamp = lock.tryConvertToWriteLock(readStamp);
            if (writeStamp == 0) {
                log("Failed to convert to write lock. Acquire the write lock.");
                if (readLockAcquired) {
                    lock.unlockRead(readStamp);
                    readLockAcquired = false;
                }
                writeStamp = lock.writeLock();
            } else {
                readLockAcquired = false;
            }

            try {
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
                lock.unlockWrite(writeStamp);
            }
        } finally {
            if (readLockAcquired) {
                log("Releasing the read lock");
                lock.unlockRead(readStamp);
            }
        }
    }

    public BigDecimal balance() {
        long stamp = lock.tryOptimisticRead(); /* Acquire optimistic read lock */
        final BigDecimal observedBalance = balance;
        if (lock.validate(stamp)) { /* Validate the stamp */
            log("Acquired optimistic read lock. Balance is " + observedBalance);
            return observedBalance;
        }

        /* Fallback to read lock if data was modified during optimistic read */
        stamp = lock.readLock();
        try {
            log("Fallback to read lock. Balance is " + balance);
            return balance;
        } finally {
            lock.unlockRead(stamp);
        }
    }
}
