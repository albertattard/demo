package demo;

public final class Main {

    private static final int HEADER_LENGTH = 20_000;
    private static final int BODY_LENGTH = Integer.MAX_VALUE - 5_000;

    static void main() {
        final BufferLimitValidator validator = new BufferLimitValidator();
        final long available = validator.availableCapacity(HEADER_LENGTH, BODY_LENGTH);

        if (!validator.canProcess(HEADER_LENGTH, BODY_LENGTH)) {
            throw new RuntimeException("I'm out of buffer! Available space = " + available);
        }

        System.out.println("Available buffer space = " + available);
    }

    private Main() {}
}
