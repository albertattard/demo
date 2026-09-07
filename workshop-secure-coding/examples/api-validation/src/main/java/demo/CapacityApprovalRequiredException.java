package demo;

/**
 * Signals that an operator must review a response before it can change reservations.
 */
public final class CapacityApprovalRequiredException extends RuntimeException {
    public CapacityApprovalRequiredException(final String message) {
        super(message);
    }
}
