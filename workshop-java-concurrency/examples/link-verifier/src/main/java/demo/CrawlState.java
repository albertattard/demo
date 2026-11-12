package demo;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Tracks discovered URLs and the work still waiting to be checked.
 */
final class CrawlState {

    enum Admission {ADDED, ALREADY_DISCOVERED, LIMIT_REACHED}

    private final Set<URI> discovered = new HashSet<>();
    private final ArrayDeque<URI> pending = new ArrayDeque<>();
    private final int maxLinks;

    CrawlState(final int maxLinks) {
        if (maxLinks < 1) {
            throw new IllegalArgumentException("Maximum number of links must be at least 1");
        }

        this.maxLinks = maxLinks;
    }

    CrawlState(final URI start, final int maxLinks) {
        this(maxLinks);
        offer(requireNonNull(start, "Start URI must not be null"));
    }

    Admission offer(final URI url) {
        requireNonNull(url, "url must not be null");
        if (discovered.contains(url)) {
            return Admission.ALREADY_DISCOVERED;
        }

        if (discovered.size() >= maxLinks) {
            return Admission.LIMIT_REACHED;
        }

        discovered.add(url);
        pending.add(url);

        return Admission.ADDED;
    }

    boolean hasPending() {
        return !pending.isEmpty();
    }

    URI removeNext() {
        return pending.remove();
    }
}
