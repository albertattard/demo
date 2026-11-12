package demo;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigurationTest {

    @Test
    void acceptsAnExplicitRateAlongsideTheUrlLimit() {
        final CrawlerConfiguration.Sequential configuration = new CrawlerConfiguration.Sequential(
                20, URI.create("https://example.com/"), 2.5,
                Duration.ofSeconds(5), Duration.ofSeconds(10));

        assertThat(configuration.maxLinks()).isEqualTo(20);
        assertThat(configuration.requestsPerSecond()).isEqualTo(2.5);
        assertThat(configuration.connectTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(configuration.requestTimeout()).isEqualTo(Duration.ofSeconds(10));
    }

    @Test
    void acceptsExplicitTimeouts() {
        final CrawlerConfiguration.Sequential configuration = new CrawlerConfiguration.Sequential(
                20, URI.create("https://example.com/"), 2.5, Duration.ofSeconds(2), Duration.ofSeconds(3));

        assertThat(configuration.connectTimeout()).isEqualTo(Duration.ofSeconds(2));
        assertThat(configuration.requestTimeout()).isEqualTo(Duration.ofSeconds(3));
    }

    @Test
    void rejectsNonPositiveTimeouts() {
        for (final Duration timeout : new Duration[]{Duration.ZERO, Duration.ofSeconds(-1)}) {
            assertThatThrownBy(() -> new CrawlerConfiguration.Sequential(
                    20, URI.create("https://example.com/"), 2.5, timeout, Duration.ofSeconds(10)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Connect timeout must be positive");
            assertThatThrownBy(() -> new CrawlerConfiguration.Sequential(
                    20, URI.create("https://example.com/"), 2.5, Duration.ofSeconds(5), timeout))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Request timeout must be positive");
        }
    }

    @Test
    void rejectsNonPositiveAndNonFiniteRates() {
        for (final double rate : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertThatThrownBy(() -> new CrawlerConfiguration.Sequential(
                    20, URI.create("https://example.com/"), rate,
                    Duration.ofSeconds(5), Duration.ofSeconds(10)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("finite and positive");
        }
    }
}
