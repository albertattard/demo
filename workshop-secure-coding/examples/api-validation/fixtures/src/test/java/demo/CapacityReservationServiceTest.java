package demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class CapacityReservationServiceTest {

    @Test
    void reservesCapacityWithinTheApprovedAutomaticRange() {
        final ReservationLedger ledger = new ReservationLedger();
        serviceFor(ledger).reserve(response(40, Instant.now()));
        assertEquals(40, ledger.reservedUnits());
    }

    @Test
    void implausibleCapacityRequiresApprovalAndDoesNotChangeReservations() {
        final ReservationLedger ledger = new ReservationLedger();
        assertThrows(CapacityApprovalRequiredException.class,
                () -> serviceFor(ledger).reserve(response(600, Instant.now())));
        assertEquals(0, ledger.reservedUnits());
    }

    @Test
    void staleCapacityRequiresApprovalAndDoesNotChangeReservations() {
        final ReservationLedger ledger = new ReservationLedger();
        assertThrows(CapacityApprovalRequiredException.class,
                () -> serviceFor(ledger).reserve(response(40, Instant.parse("2025-09-06T09:00:00Z"))));
        assertEquals(0, ledger.reservedUnits());
    }

    private CapacityReservationService serviceFor(final ReservationLedger ledger) {
        return new CapacityReservationService(ledger);
    }

    private CapacityResponse response(final int availableUnits, final Instant observedAt) {
        return new CapacityResponse(
                "eu-frankfurt-1",
                "compute-standard",
                availableUnits,
                observedAt);
    }
}
