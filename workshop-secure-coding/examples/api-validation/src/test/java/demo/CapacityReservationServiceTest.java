package demo;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CapacityReservationServiceTest {

    @Test
    void reservesTheCapacityReportedByTheApi() {
        final ReservationLedger ledger = new ReservationLedger();
        final CapacityResponse response = new CapacityResponse(
                "eu-frankfurt-1",
                "compute-standard",
                40,
                Instant.parse("2026-09-06T09:00:00Z"));
        new CapacityReservationService(ledger).reserve(response);
        assertEquals(40, ledger.reservedUnits());
    }
}
