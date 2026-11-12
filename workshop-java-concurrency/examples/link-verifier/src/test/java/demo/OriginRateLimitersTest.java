package demo;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class OriginRateLimitersTest {

    @Test
    void sharesOneLimiterAcrossUrlsFromTheSameOrigin() {
        final OriginRateLimiters limiters = new OriginRateLimiters(2.5);
        final var limiter = limiters.forUrl(URI.create("http://example.com/start"));

        assertThat(limiters.forUrl(URI.create("HTTP://EXAMPLE.COM:80/other?query=1")))
                .isSameAs(limiter);
        assertThat(limiter.getRate()).isEqualTo(2.5);
    }

    @Test
    void givesDifferentOriginsIndependentLimitersAtTheConfiguredRate() {
        final OriginRateLimiters limiters = new OriginRateLimiters(2.5);
        final var first = limiters.forUrl(URI.create("http://example.com/"));
        final var second = limiters.forUrl(URI.create("http://example.com:8080/"));

        assertThat(second).isNotSameAs(first);
        first.setRate(1.0);
        assertThat(second.getRate()).isEqualTo(2.5);
    }

    @Test
    void doesNotShareLimitersBetweenCrawls() {
        final URI url = URI.create("https://example.com/");
        final OriginRateLimiters first = new OriginRateLimiters(2.5);
        final OriginRateLimiters second = new OriginRateLimiters(2.5);

        assertThat(first.forUrl(url)).isNotSameAs(second.forUrl(url));
    }
}
