package demo;

import java.math.BigDecimal;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static demo.utils.Logs.log;
import static demo.utils.Logs.logHeaderAndJavaVersion;
import static demo.utils.Logs.logSection;

public final class Main {

    public static void main(final String[] args) {
        logHeaderAndJavaVersion("Synchronization");

        logSection("Deposit and withdraw money using several threads");

        final BankAccount account = new BankAccount();
        log("Balance before all threads start: " + account.balance());

        /* Deposit and withdraw money using several threads */
        CompletableFuture.allOf(
                        IntStream.range(0, 12)
                                .mapToObj(Main::createAdjustment)
                                .map(adjustment -> (Runnable) () -> account.adjustBy(adjustment))
                                .map(CompletableFuture::runAsync)
                                .toArray(CompletableFuture[]::new))
                .join();

        log("Balance after all threads finish: " + account.balance());
    }

    private static BigDecimal createAdjustment(final int i) {
        return i % 2 == 0
                ? BigDecimal.valueOf(100) /* Deposit */
                : BigDecimal.valueOf(-100) /* Withdrawal */;
    }
}
