package demo;

import java.net.URI;
import java.time.Duration;

import static java.util.Objects.requireNonNull;

sealed interface CrawlerConfiguration {

    record Sequential(
            int maxLinks,
            URI start,
            double requestsPerSecond,
            Duration connectTimeout,
            Duration requestTimeout) implements CrawlerConfiguration {

        public Sequential {
            requireValidRequestsPerSecond(requestsPerSecond);
            requireValidMaxLinks(maxLinks);
            start = requireValidStartUri(start);
            requireValidTimeout(connectTimeout, "Connect timeout");
            requireValidTimeout(requestTimeout, "Request timeout");
        }
    }

    record Concurrent(
            int maxLinks,
            URI start,
            double requestsPerSecond,
            Duration connectTimeout,
            Duration requestTimeout,
            int workers) implements CrawlerConfiguration {

        public Concurrent {
            requireValidRequestsPerSecond(requestsPerSecond);
            requireValidMaxLinks(maxLinks);
            start = requireValidStartUri(start);
            requireValidTimeout(connectTimeout, "Connect timeout");
            requireValidTimeout(requestTimeout, "Request timeout");
            requireValidWorkers(workers);
        }

        private static void requireValidWorkers(final int workers) {
            if (workers < 1) {
                throw new IllegalArgumentException("Workers must be at least 1");
            }
        }
    }

    private static void requireValidTimeout(final Duration timeout, final String name) {
        requireNonNull(timeout, name);
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    private static void requireValidRequestsPerSecond(final double requestsPerSecond) {
        if (!Double.isFinite(requestsPerSecond) || requestsPerSecond <= 0) {
            throw new IllegalArgumentException("Requests per second per origin must be finite and positive");
        }
    }

    private static void requireValidMaxLinks(final int maxLinks) {
        if (maxLinks < 1) {
            throw new IllegalArgumentException("Maximum number of links must be at least 1");
        }
    }

    private static URI requireValidStartUri(final URI start) {
        final URI normalised = Links.resource(start);
        if (Links.isNotHttp(normalised)) {
            throw new IllegalArgumentException("Starting URL must be an absolute HTTP or HTTPS URL");
        }
        return normalised;
    }
}
