package demo;

import module java.base;

record CrawlResult(List<Outcome> outcomes, List<String> diagnostics, boolean limitReached) {

    CrawlResult {
        outcomes = List.copyOf(outcomes);
        diagnostics = List.copyOf(diagnostics);
    }

    sealed interface Outcome {
        URI url();
    }

    // Redirects without a usable HTTP(S) target remain responses with a diagnostic.
    record Response(URI url, int status) implements Outcome {}

    record Redirect(URI url, int status, URI target) implements Outcome {}

    record Failure(URI url, String message) implements Outcome {}

    static Builder builder() {
        return new Builder();
    }

    static final class Builder {
        private final List<Outcome> outcomes = new ArrayList<>();
        private final List<String> diagnostics = new ArrayList<>();
        private boolean limitReached;

        private Builder() {}

        Builder response(final URI url, final int status) {
            return outcome(new CrawlResult.Response(url, status));
        }

        Builder redirect(final URI url, final int status, final URI target) {
            return outcome(new CrawlResult.Redirect(url, status, target));
        }

        Builder failure(final URI url, final String message) {
            return outcome(new CrawlResult.Failure(url, message));
        }

        Builder outcome(final Outcome outcome) {
            this.outcomes.add(outcome);
            return this;
        }

        Builder diagnostic(final String diagnostic) {
            this.diagnostics.add(diagnostic);
            return this;
        }

        Builder diagnostics(final Collection<String> diagnostics) {
            this.diagnostics.addAll(diagnostics);
            return this;
        }

        Builder limitReached() {
            this.limitReached = true;
            return this;
        }

        CrawlResult build() {
            return new CrawlResult(outcomes, diagnostics, limitReached);
        }
    }
}
