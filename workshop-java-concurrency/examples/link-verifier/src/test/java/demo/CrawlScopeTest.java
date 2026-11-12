package demo;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class CrawlScopeTest {

    @Test
    void limitsExplorationToOriginAndPathBoundary() {
        final CrawlScope scope = new CrawlScope(URI.create("https://example.com/demo/"));
        assertThat(scope.contains(URI.create("https://example.com/demo/guide"))).isTrue();
        assertThat(scope.contains(URI.create("https://example.com/demonstration"))).isFalse();
        assertThat(scope.contains(URI.create("https://example.com:444/demo/guide"))).isFalse();
    }
}
