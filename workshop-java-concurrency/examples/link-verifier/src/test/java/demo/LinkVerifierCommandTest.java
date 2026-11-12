package demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(5)
class LinkVerifierCommandTest {

    @Test
    void usesDefaultsWhenOnlyTheUrlIsSupplied() {
        final LinkVerifierCommand command = new LinkVerifierCommand();
        new CommandLine(command).parseArgs("https://example.com/");
        assertThat(command.configuration()).isInstanceOf(CrawlerConfiguration.Sequential.class);
        final CrawlerConfiguration.Sequential configuration = (CrawlerConfiguration.Sequential) command.configuration();

        assertThat(configuration.start()).isEqualTo(URI.create("https://example.com/"));
        assertThat(configuration.maxLinks()).isEqualTo(100);
        assertThat(configuration.requestsPerSecond()).isEqualTo(5.0);
        assertThat(configuration.connectTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(configuration.requestTimeout()).isEqualTo(Duration.ofSeconds(10));
    }

    @Test
    void acceptsExplicitSequentialMode() {
        final LinkVerifierCommand command = new LinkVerifierCommand();
        new CommandLine(command).parseArgs("--mode", "sequential", "https://example.com/");
        assertThat(command.configuration()).isInstanceOf(CrawlerConfiguration.Sequential.class);
    }

    @Test
    void acceptsConcurrentModeAndWorkerCount() {
        final LinkVerifierCommand command = new LinkVerifierCommand();
        new CommandLine(command).parseArgs("--mode", "concurrent", "--workers", "3", "https://example.com/");
        assertThat(command.configuration()).isEqualTo(new CrawlerConfiguration.Concurrent(
                100, URI.create("https://example.com/"), 5.0,
                Duration.ofSeconds(5), Duration.ofSeconds(10), 3));
        assertUsageError("Workers must be at least 1", "--mode", "concurrent", "--workers", "0", "https://example.com/");
    }

    @Test
    void acceptsOptionsIndependentlyAndInEitherOrder() {
        final CrawlerConfiguration.Sequential rateOnly = parse("https://example.com/",
                "--requests-per-second-per-origin", "2.5");
        final CrawlerConfiguration.Sequential limitOnly = parse("--max-links", "20", "https://example.com/");
        final CrawlerConfiguration.Sequential both = parse("--requests-per-second-per-origin", "2.5",
                "https://example.com/", "--max-links", "20");

        assertThat(rateOnly.maxLinks()).isEqualTo(100);
        assertThat(rateOnly.requestsPerSecond()).isEqualTo(2.5);
        assertThat(limitOnly.maxLinks()).isEqualTo(20);
        assertThat(limitOnly.requestsPerSecond()).isEqualTo(5.0);
        assertThat(both).isEqualTo(new CrawlerConfiguration.Sequential(
                20, URI.create("https://example.com/"), 2.5,
                Duration.ofSeconds(5), Duration.ofSeconds(10)));
    }

    @Test
    void acceptsTimeoutOptionsIndependentlyAndTogether() {
        final CrawlerConfiguration.Sequential connectOnly = parse("--connect-timeout", "PT2S", "https://example.com/");
        final CrawlerConfiguration.Sequential requestOnly = parse("https://example.com/", "--request-timeout", "PT0.5S");
        final CrawlerConfiguration.Sequential both = parse("--request-timeout", "PT3S",
                "https://example.com/", "--connect-timeout", "PT2S");

        assertThat(connectOnly.connectTimeout()).isEqualTo(Duration.ofSeconds(2));
        assertThat(connectOnly.requestTimeout()).isEqualTo(Duration.ofSeconds(10));
        assertThat(requestOnly.connectTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(requestOnly.requestTimeout()).isEqualTo(Duration.ofMillis(500));
        assertThat(both.connectTimeout()).isEqualTo(Duration.ofSeconds(2));
        assertThat(both.requestTimeout()).isEqualTo(Duration.ofSeconds(3));
    }

    @Test
    void showsHelpWithoutRequiringAUrlOrStartingACrawl() {
        for (final String help : new String[]{"--help", "-h"}) {
            final Execution result = execute(help);

            assertThat(result.exitCode()).isZero();
            assertThat(result.errors()).isEmpty();
            assertThat(result.output()).contains("Usage: link-verifier", "--max-links",
                    "--requests-per-second-per-origin", "100", "5.0",
                    "--connect-timeout", "--request-timeout", "Default: PT5S", "Default: PT10S");
        }
    }

    @Test
    void rejectsInvalidArgumentsWithUsageAndNoStackTrace() {
        assertUsageError("Missing required parameter");
        assertUsageError("Unknown option", "https://example.com/", "--unknown");
        assertUsageError("Invalid value", "https://example.com/", "--mode", "invalid");
        assertUsageError("Unmatched argument", "https://example.com/", "20");
        assertUsageError("Missing required parameter", "https://example.com/", "--max-links");
        assertUsageError("Invalid value", "https://example.com/", "--max-links", "many");
        assertUsageError("Invalid value", "https://example.com/", "--requests-per-second-per-origin", "fast");
        assertUsageError("Invalid value", "http://[invalid");
        assertUsageError("absolute HTTP or HTTPS", "file:///tmp/page.html");
        assertUsageError("absolute HTTP or HTTPS", "/relative");
        assertUsageError("at least 1", "https://example.com/", "--max-links", "0");
        for (final String rate : new String[]{"0", "-1", "NaN", "Infinity", "-Infinity"}) {
            assertUsageError("finite and positive", "https://example.com/",
                    "--requests-per-second-per-origin", rate);
        }
        for (final String option : new String[]{"--connect-timeout", "--request-timeout"}) {
            assertUsageError("Missing required parameter", "https://example.com/", option);
            assertUsageError("Invalid value", "https://example.com/", option, "invalid");
            for (final String timeout : new String[]{"PT0S", "PT-1S"}) {
                assertUsageError("must be positive", "https://example.com/", option, timeout);
            }
        }
    }

    private static CrawlerConfiguration.Sequential parse(final String... args) {
        final LinkVerifierCommand command = new LinkVerifierCommand();
        new CommandLine(command).parseArgs(args);
        return (CrawlerConfiguration.Sequential) command.configuration();
    }

    private static void assertUsageError(final String message, final String... args) {
        final Execution result = execute(args);

        assertThat(result.exitCode()).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(result.output()).isEmpty();
        assertThat(result.errors()).contains(message, "Usage: link-verifier")
                .doesNotContain("\tat ");
    }

    private static Execution execute(final String... args) {
        final StringWriter output = new StringWriter();
        final StringWriter errors = new StringWriter();
        final int exitCode = new CommandLine(new LinkVerifierCommand())
                .setOut(new PrintWriter(output))
                .setErr(new PrintWriter(errors))
                .execute(args);
        return new Execution(exitCode, output.toString(), errors.toString());
    }

    private record Execution(int exitCode, String output, String errors) {}
}
