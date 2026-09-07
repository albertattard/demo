package demo;

/**
 * Records the capacity this application has committed to reserve.
 */
public final class ReservationLedger {

    private int reservedUnits;

    public void reserve(final int units) {
        reservedUnits += units;
    }

    public int reservedUnits() {
        return reservedUnits;
    }
}
