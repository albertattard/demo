package demo;

import java.nio.file.Path;

/**
 * Runs the workshop-only capacity reservation demonstration.
 */
public final class Main {

    static void main(final String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java -jar target/api-validation-1.0.0.jar <api-response.csv>");
            System.exit(2);
        }

        final CapacityResponse response = CapacityApi.read(Path.of(args[0]));
        final ReservationLedger ledger = new ReservationLedger();
        new CapacityReservationService(ledger).reserve(response);
        System.out.printf("Reserved %d %s units in %s.%n",
                ledger.reservedUnits(), response.resourceType(), response.region());
    }

    private Main() {}
}
