package demo;

import java.math.BigDecimal;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class AtomicReferenceExampleTest {

    @Test
    void shouldEventuallyApplyAllSubmittedAdjustments() throws Exception {
        final int numberOfAdjustments = 100;

        final ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            final BankAccount account = new BankAccount(executor);

            IntStream.range(0, numberOfAdjustments)
                    .forEach(ignored -> account.adjustByAsync(BigDecimal.ONE));

            /* The API is fire-and-forget, so await its public eventual state. */
            await().atMost(1, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertThat(account.balance())
                            .as("balance after applying all submitted adjustments")
                            .isEqualByComparingTo(BigDecimal.valueOf(numberOfAdjustments)));
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(1, TimeUnit.SECONDS)).isTrue();
        }
    }

    private static final class BankAccount {

        private final AtomicReference<BigDecimal> balance = new AtomicReference<>(BigDecimal.ZERO);
        private final Executor executor;

        BankAccount(final Executor executor) {
            this.executor = requireNonNull(executor, "The executor cannot be null");
        }

        void adjustByAsync(final BigDecimal adjustment) {
            requireNonNull(adjustment, "The adjustment cannot be null");
            executor.execute(() -> adjustBy(adjustment));
        }

        BigDecimal balance() {
            return balance.get();
        }

        private void adjustBy(final BigDecimal adjustment) {
            while (true) {
                final BigDecimal current = balance.get();
                final BigDecimal updated = current.add(adjustment);
                if (balance.compareAndSet(current, updated)) {
                    return;
                }
            }
        }
    }
}
