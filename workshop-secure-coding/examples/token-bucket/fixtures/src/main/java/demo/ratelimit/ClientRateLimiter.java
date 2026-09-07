package demo.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * Workshop-only, single-instance admission control. Production deployments need a
 * gateway or shared store to enforce the same policy across every application node.
 */
@Service
public final class ClientRateLimiter {

    private static final int MAX_TRACKED_CLIENTS = 1_000;
    private static final long CAPACITY = 20;

    private static final Bandwidth LIMIT = Bandwidth.builder()
            .capacity(CAPACITY)
            .refillGreedy(CAPACITY, Duration.ofMinutes(1))
            .build();

    private final Map<String, Bucket> buckets = new HashMap<>();

    public boolean tryConsume(final String clientId, final long cost) {
        if (cost <= 0 || cost > CAPACITY) {
            throw new IllegalArgumentException("Cost must be between 1 and " + CAPACITY);
        }

        return bucketFor(clientId)
                .tryConsume(cost);
    }

    private synchronized Bucket bucketFor(final String clientId) {
        Bucket bucket = buckets.get(clientId);
        if (bucket == null) {
            if (buckets.size() >= MAX_TRACKED_CLIENTS) {
                return null;
            }
            bucket = Bucket.builder().addLimit(LIMIT).build();
            buckets.put(clientId, bucket);
        }

        return bucket;
    }
}
