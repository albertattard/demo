package demo;

import java.time.Duration;
import java.time.Instant;

/**
 * Workshop policy for deciding whether an API response may create an automatic reservation.
 */
public final class CapacityReservationPolicy {

    private static final int TRUSTED_BASELINE_UNITS = 40;
    private static final int MAXIMUM_AUTOMATIC_INCREASE = 20;
    private static final Duration MAXIMUM_RESPONSE_AGE = Duration.ofMinutes(15);

    public void requireAutomaticReservationIsSafe(final CapacityResponse response, final Instant now) {
        if (response.availableUnits() < 0) {
            throw new CapacityApprovalRequiredException("Negative capacity requires review.");
        }

        if (response.availableUnits() > TRUSTED_BASELINE_UNITS + MAXIMUM_AUTOMATIC_INCREASE) {
            throw new CapacityApprovalRequiredException(
                    "Reported capacity exceeds the approved automatic-reservation range.");
        }

        if (response.observedAt().isBefore(now.minus(MAXIMUM_RESPONSE_AGE))) {
            throw new CapacityApprovalRequiredException("Stale capacity requires review.");
        }
    }
}
