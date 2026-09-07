package demo;

import java.time.Clock;

/**
 * Applies the explicit automatic-reservation policy before changing the ledger.
 */
public final class CapacityReservationService {
    private final ReservationLedger ledger;
    private final CapacityReservationPolicy policy;
    private final Clock clock;

    public CapacityReservationService(final ReservationLedger ledger) {
        this(ledger, new CapacityReservationPolicy(), Clock.systemUTC());
    }

    CapacityReservationService(final ReservationLedger ledger, final CapacityReservationPolicy policy, final Clock clock) {
        this.ledger = ledger;
        this.policy = policy;
        this.clock = clock;
    }

    public void reserve(final CapacityResponse response) {
        policy.requireAutomaticReservationIsSafe(response, clock.instant());
        ledger.reserve(response.availableUnits());
    }
}
