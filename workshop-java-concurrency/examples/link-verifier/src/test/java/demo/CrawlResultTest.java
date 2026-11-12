package demo;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class CrawlResultTest {

    @Test
    void builtResultIsASnapshotOfTheBuilder() {
        final CrawlResult.Response response = new CrawlResult.Response(
                URI.create("https://example.com/"), 200);
        final CrawlResult.Builder builder = CrawlResult.builder().outcome(response).diagnostic("first");

        final CrawlResult result = builder.build();
        builder.outcome(new CrawlResult.Failure(URI.create("https://example.com/other"), "failed"))
                .diagnostic("second")
                .limitReached();

        assertThat(result.outcomes()).containsExactly(response);
        assertThat(result.diagnostics()).containsExactly("first");
        assertThat(result.limitReached()).isFalse();
        assertThat(builder.build().limitReached()).isTrue();
    }
}
