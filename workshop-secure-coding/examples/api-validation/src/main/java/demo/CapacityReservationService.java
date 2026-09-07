package demo;

/**
 * Deliberately vulnerable: a parseable response is sufficient authority to reserve capacity.
 */
public final class CapacityReservationService {

    private final ReservationLedger ledger;

    public CapacityReservationService(final ReservationLedger ledger) {
        this.ledger = ledger;
    }

    public void reserve(final CapacityResponse response) {
        ledger.reserve(response.availableUnits());
    }
}
