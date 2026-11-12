package demo;

import java.math.BigDecimal;

import static demo.utils.Logs.log;
import static java.util.Objects.requireNonNull;

public final class BankAccount {

    /* Protected by this object's monitor (synchronized) */
    private BigDecimal balance = BigDecimal.ZERO;

    public synchronized void adjustBy(final BigDecimal adjustment) {
        requireNonNull(adjustment);

        log("The amount of " + balance + " is adjusted by: " + adjustment);
        balance = balance.add(adjustment);
        log("The balance after adjustment " + balance);
    }

    public BigDecimal balance() {
        synchronized (this) {
            return balance;
        }
    }
}
