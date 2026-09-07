package demo;

import java.time.Instant;

/**
 * A response received from the inventory API outside this application's trust boundary.
 */
public record CapacityResponse(
        String region,
        String resourceType,
        int availableUnits,
        Instant observedAt) {}
