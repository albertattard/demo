package demo;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReentrantLockExampleTest {

    @Test
    void shouldApplyAllConcurrentDeposits() throws Exception {
        final int numberOfDeposits = 12;
        final BigDecimal deposit = BigDecimal.valueOf(100);
        final ReentrantLockExample.BankAccount account = new ReentrantLockExample.BankAccount();

        CompletableFuture.allOf(
                        IntStream.range(0, numberOfDeposits)
                                .mapToObj(ignored -> CompletableFuture.runAsync(
                                        () -> account.adjustBy(deposit)))
                                .toArray(CompletableFuture[]::new))
                .get(1, TimeUnit.SECONDS);

        assertThat(account.balance())
                .as("balance after all deposits complete")
                .isEqualByComparingTo(BigDecimal.valueOf(1200));
    }
}
