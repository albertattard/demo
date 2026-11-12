package demo;

import java.io.Serial;

public final class WithdrawMoneyException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public WithdrawMoneyException(final String message) {
        super(message);
    }
}