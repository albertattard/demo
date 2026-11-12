package demo;

import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

public final class FaultyWorker implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                transferMoney();
            } catch (final RuntimeException e) {
                /* Swallow the exception! */
            }

            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void transferMoney() {
        if (!hasEnoughMoney()) {
            throw new WithdrawMoneyException("Not enough money in account");
        }

        /* Transfer money */
    }

    private boolean hasEnoughMoney() {
        return RandomGenerator.getDefault().nextBoolean();
    }
}
