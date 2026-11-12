package demo;

import java.net.URI;

import static demo.Links.port;

final class CrawlScope {

    private final URI start;
    private final String root;

    CrawlScope(final URI start) {
        this.start = start;
        final String path = start.getRawPath();
        this.root = path == null || path.isEmpty() || path.equals("/")
                ? "/"
                : path.replaceAll("/+$", "");
    }

    boolean contains(final URI candidate) {
        final String path = candidate.getRawPath().isEmpty() ? "/" : candidate.getRawPath();
        return start.getScheme().equalsIgnoreCase(candidate.getScheme())
                && start.getHost().equalsIgnoreCase(candidate.getHost())
                && port(start) == port(candidate)
                && (root.equals("/") || path.equals(root) || path.startsWith(root + "/"));
    }
}
