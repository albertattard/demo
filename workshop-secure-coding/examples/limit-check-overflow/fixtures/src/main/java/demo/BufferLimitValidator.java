package demo;

public final class BufferLimitValidator {

    private static final long MAX_BUFFER_CAPACITY = 10_000;

    public long availableCapacity(final int headerLength, final int bodyLength) {
        if (headerLength < 0 || bodyLength < 0) {
            return -1;
        }

        return MAX_BUFFER_CAPACITY - headerLength - bodyLength;
    }

    public boolean canProcess(final int headerLength, final int bodyLength) {
        return availableCapacity(headerLength, bodyLength) >= 0;
    }
}
