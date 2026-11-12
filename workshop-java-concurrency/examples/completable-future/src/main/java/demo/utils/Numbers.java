package demo.utils;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public final class Numbers {

    public static int requirePositive(final int value) {
        return requirePositive(value, "The value cannot be negative");
    }

    public static int requirePositive(final int value, final String errorMessage) {
        if (isNegative(value)) {
            throw new IllegalArgumentException(errorMessage);
        }

        return value;
    }

    public static BigDecimal requirePositive(final BigDecimal value) {
        if (value == null || isNegative(value)) {
            throw new IllegalArgumentException("The value cannot be null or negative");
        }

        return value;
    }

    public static int requireGreaterThanZero(final int value) {
        return requireGreaterThanZero(value, "The value cannot be less than zero");
    }

    public static int requireGreaterThanZero(final int value, final String errorMessage) {
        if (value <= 0) {
            throw new IllegalArgumentException(errorMessage);
        }

        return value;
    }

    public static boolean isNegative(final int value) {
        return value < 0;
    }

    public static boolean isNegative(final BigDecimal value) {
        requireNonNull(value, "The value cannot be null");
        return value.compareTo(BigDecimal.ZERO) < 0;
    }

    private Numbers() {}
}
