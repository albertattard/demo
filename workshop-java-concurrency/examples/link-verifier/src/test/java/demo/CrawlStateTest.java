package demo;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrawlStateTest {

    @Test
    void startingUrlIsDiscoveredAndCountsTowardsLimit() {
        final URI start = URI.create("https://example.com/");
        final CrawlState state = new CrawlState(start, 1);

        assertThat(state.removeNext()).isEqualTo(start);
        assertThat(state.offer(start)).isEqualTo(CrawlState.Admission.ALREADY_DISCOVERED);
        assertThat(state.offer(URI.create("https://example.com/other")))
                .isEqualTo(CrawlState.Admission.LIMIT_REACHED);
        assertThat(state.hasPending()).isFalse();
    }

    @Test
    void retainsDiscoveredUrlsAfterProcessingAndLimitsNewUrls() {
        final CrawlState state = new CrawlState(2);
        final URI first = URI.create("https://example.com/first");
        final URI second = URI.create("https://example.com/second");
        final URI third = URI.create("https://example.com/third");

        assertThat(state.offer(first)).isEqualTo(CrawlState.Admission.ADDED);
        assertThat(state.offer(second)).isEqualTo(CrawlState.Admission.ADDED);
        assertThat(state.removeNext()).isEqualTo(first);
        assertThat(state.offer(first)).isEqualTo(CrawlState.Admission.ALREADY_DISCOVERED);
        assertThat(state.offer(third)).isEqualTo(CrawlState.Admission.LIMIT_REACHED);
        assertThat(state.removeNext()).isEqualTo(second);
        assertThat(state.hasPending()).isFalse();
    }

    @Test
    void reportsLimitOnlyAfterRejectingADistinctUrl() {
        final URI start = URI.create("https://example.com/");
        final CrawlState state = new CrawlState(start, 1);

        assertThat(state.offer(start)).isEqualTo(CrawlState.Admission.ALREADY_DISCOVERED);
        assertThat(state.offer(URI.create("https://example.com/other")))
                .isEqualTo(CrawlState.Admission.LIMIT_REACHED);
        assertThat(state.removeNext()).isEqualTo(start);
        assertThat(state.offer(start)).isEqualTo(CrawlState.Admission.ALREADY_DISCOVERED);
    }

    @Test
    void rejectsNonPositiveLimits() {
        assertThatThrownBy(() -> new CrawlState(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
