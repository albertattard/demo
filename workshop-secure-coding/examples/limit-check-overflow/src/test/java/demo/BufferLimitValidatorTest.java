package demo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BufferLimitValidatorTest {

    private final BufferLimitValidator validator = new BufferLimitValidator();

    @Test
    void acceptsAnInboundMessageThatExactlyFillsTheBuffer() {
        assertTrue(validator.canProcess(4_000, 6_000));
    }

    @Test
    void rejectsAnInboundMessageThatExceedsTheBuffer() {
        assertFalse(validator.canProcess(4_000, 6_001));
    }
}
