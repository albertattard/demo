package demo;

public final class BufferLimitValidator {

    private static final long MAX_BUFFER_CAPACITY = 10_000;

    public long availableCapacity(final int headerLength, final int bodyLength) {
        // BUG: both operands are int, so this addition overflows before the result
        // is widened to long.
        final long inbound = headerLength + bodyLength;
        return MAX_BUFFER_CAPACITY - inbound;
    }

    public boolean canProcess(final int headerLength, final int bodyLength) {
        return availableCapacity(headerLength, bodyLength) >= 0;
    }
}
