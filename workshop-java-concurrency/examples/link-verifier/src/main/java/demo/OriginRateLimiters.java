package demo;

import com.google.common.util.concurrent.RateLimiter;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Owned by one crawl and accessed only by its coordinator.
 */
final class OriginRateLimiters {

    private final Map<Origin, RateLimiter> limiters = new HashMap<>();
    private final double requestsPerSecond;

    OriginRateLimiters(final double requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
    }

    RateLimiter forUrl(final URI url) {
        return limiters.computeIfAbsent(Origin.of(url),
                origin -> RateLimiter.create(requestsPerSecond));
    }
}
