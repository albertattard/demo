package demo;

import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

import java.net.URI;
import java.time.Duration;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.Callable;

@Command(name = "link-verifier", description = "Check website links.", showDefaultValues = true)
final class LinkVerifierCommand implements Callable<Integer> {

    private enum Mode {sequential, concurrent}

    @Parameters(index = "0", paramLabel = "URL", description = "Starting HTTP or HTTPS URL.")
    private URI start;

    @Option(names = "--mode", defaultValue = "sequential", paramLabel = "MODE", description = "Crawl mode: ${COMPLETION-CANDIDATES}.")
    private Mode mode;

    @Option(names = "--workers", defaultValue = "5", paramLabel = "COUNT", description = "Maximum concurrent checks.")
    private int workers;

    @Option(names = "--max-links", defaultValue = "100", paramLabel = "COUNT", description = "Maximum number of distinct URLs to check.")
    private int maxLinks;

    @Option(names = "--requests-per-second-per-origin", defaultValue = "5.0", paramLabel = "RATE", description = "Request rate per origin; bursts are possible after inactivity.")
    private double requestsPerSecond;

    @Option(names = "--connect-timeout", defaultValue = "PT5S", paramLabel = "DURATION", description = "Connection timeout as an ISO-8601 duration (e.g. PT5S).")
    private Duration connectTimeout;

    @Option(names = "--request-timeout", defaultValue = "PT10S", paramLabel = "DURATION", description = "Request timeout as an ISO-8601 duration (e.g. PT10S).")
    private Duration requestTimeout;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
    private boolean helpRequested;

    @Spec
    private CommandSpec spec;

    CrawlerConfiguration configuration() {
        try {
            return switch (mode) {
                case sequential ->
                        new CrawlerConfiguration.Sequential(maxLinks, start, requestsPerSecond, connectTimeout, requestTimeout);
                case concurrent ->
                        new CrawlerConfiguration.Concurrent(maxLinks, start, requestsPerSecond, connectTimeout, requestTimeout, workers);
            };
        } catch (final IllegalArgumentException e) {
            throw new ParameterException(spec.commandLine(), e.getMessage(), e);
        }
    }

    @Override
    public Integer call() throws InterruptedException {
        final CrawlerConfiguration configuration = configuration();
        final CrawlResult result;
        final long started = System.nanoTime();
        try {
            result = switch (configuration) {
                case CrawlerConfiguration.Sequential sequential -> new SequentialCrawler().crawl(sequential);
                case CrawlerConfiguration.Concurrent concurrent -> new ConcurrentCrawler().crawl(concurrent);
            };
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
        final long elapsedNanos = System.nanoTime() - started;

        final var out = spec.commandLine().getOut();
        final var err = spec.commandLine().getErr();
        result.outcomes().stream()
                .sorted(Comparator.comparing(outcome -> outcome.url().toString()))
                .forEach(outcome -> {
                    switch (outcome) {
                        case CrawlResult.Response response -> out.println(response.status() + " " + response.url());
                        case CrawlResult.Redirect redirect -> {
                            out.println(redirect.status() + " " + redirect.url());
                            out.println("  -> " + redirect.target());
                        }
                        case CrawlResult.Failure failure ->
                                err.println("Failed " + failure.url() + ": " + failure.message());
                    }
                });
        result.diagnostics().forEach(err::println);
        if (result.limitReached()) {
            out.println("URL limit reached; results are incomplete");
        }
        out.printf(Locale.ROOT, "Crawl completed in %.3f s%n", elapsedNanos / 1_000_000_000.0);
        return 0;
    }
}
