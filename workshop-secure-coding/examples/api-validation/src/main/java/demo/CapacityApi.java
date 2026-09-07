package demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Workshop-only adapter for a remote inventory API response.
 */
public final class CapacityApi {

    public static CapacityResponse read(final Path responseFile) throws IOException {
        final String[] fields = Files.readString(responseFile).trim().split(",", -1);
        if (fields.length != 4) {
            throw new IllegalArgumentException("Expected region, resource type, available units, observed-at");
        }

        final String observedAt = fields[3].trim();
        return new CapacityResponse(
                fields[0].trim(),
                fields[1].trim(),
                Integer.parseInt(fields[2].trim()),
                "now".equals(observedAt) ? Instant.now() : Instant.parse(observedAt));
    }

    private CapacityApi() {}
}
