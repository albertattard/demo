package demo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static demo.WireMockTestSupport.withServer;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@Timeout(10)
class ConcurrentCrawlerTest {

    @Test
    void overlapsIndependentRequests() throws Exception {
        final CountDownLatch requestsStarted = new CountDownLatch(2);
        final CountDownLatch releaseResponses = new CountDownLatch(1);
        final var crawlExecutor = Executors.newSingleThreadExecutor();
        final ResponseDefinitionTransformerV2 holdResponses = new ResponseDefinitionTransformerV2() {
            @Override
            public String getName() {
                return "hold-responses";
            }

            @Override
            public ResponseDefinition transform(final ServeEvent event) {
                if (!event.getRequest().getUrl().equals("/")) {
                    requestsStarted.countDown();
                    try {
                        if (!releaseResponses.await(5, TimeUnit.SECONDS)) {
                            return aResponse().withStatus(500).build();
                        }
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return aResponse().withStatus(500).build();
                    }
                }
                return event.getResponseDefinition();
            }
        };
        final WireMockServer server = new WireMockServer(wireMockConfig().dynamicPort().extensions(holdResponses));
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/"))
                    .willReturn(aResponse().withHeader("Content-Type", "text/html")
                            .withBody("<a href='/one'>One</a><a href='/two'>Two</a>")));
            server.stubFor(get(urlMatching("/one|/two")).willReturn(aResponse().withStatus(200)));
            final URI start = URI.create(server.baseUrl() + "/");
            final var crawl = crawlExecutor.submit(() -> new ConcurrentCrawler().crawl(
                    new CrawlerConfiguration.Concurrent(10, start, 1000,
                            Duration.ofSeconds(2), Duration.ofSeconds(8), 2)));
            try {
                assertThat(requestsStarted.await(4, TimeUnit.SECONDS))
                        .as("both independent requests start before either response is released")
                        .isTrue();
            } finally {
                releaseResponses.countDown();
            }
            assertThat(crawl.get(4, TimeUnit.SECONDS).outcomes()).hasSize(3);
        } finally {
            releaseResponses.countDown();
            server.stop();
            crawlExecutor.shutdownNow();
            assertThat(crawlExecutor.awaitTermination(2, TimeUnit.SECONDS)).isTrue();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 302})
    void doesNotReportLimitForKnownUrlsAtCapacity(final int status) throws Exception {
        withServer(server -> {
            server.stubFor(get(urlPathEqualTo("/start"))
                    .willReturn(aResponse().withStatus(status)
                            .withHeader("Content-Type", "text/html")
                            .withHeader("Location", "/start")
                            .withBody("<a href='/start'>Self</a>")));
            final URI start = URI.create(server.baseUrl() + "/start");
            final CrawlResult result = new ConcurrentCrawler().crawl(new CrawlerConfiguration.Concurrent(1, start, 1000,
                    Duration.ofSeconds(2), Duration.ofSeconds(2), 5));

            assertThat(result.outcomes()).hasSize(1);
            assertThat(result.limitReached()).isFalse();
            server.verify(1, getRequestedFor(urlPathEqualTo("/start")));
        });
    }

    @Test
    void recordsRedirectEvenWhenTargetExceedsLimit() throws Exception {
        withServer(server -> {
            server.stubFor(get(urlPathEqualTo("/start"))
                    .willReturn(aResponse().withStatus(301).withHeader("Location", "/next")));
            final String root = server.baseUrl();
            final CrawlResult result = new ConcurrentCrawler().crawl(new CrawlerConfiguration.Concurrent(1, URI.create(root + "/start"), 1000,
                    Duration.ofSeconds(2), Duration.ofSeconds(2), 5));
            assertThat(result.outcomes()).containsExactly(new CrawlResult.Redirect(URI.create(root + "/start"), 301, URI.create(root + "/next")));
            assertThat(result.limitReached()).isTrue();
            server.verify(0, getRequestedFor(urlPathEqualTo("/next")));
        });
    }

    @Test
    void followsRedirectAndReportsUnusableLocation() throws Exception {
        withServer(server -> {
            server.stubFor(get(urlPathEqualTo("/start"))
                    .willReturn(aResponse().withStatus(302).withHeader("Location", "/next")));
            server.stubFor(get(urlPathEqualTo("/next")).willReturn(aResponse().withStatus(301)));
            final String root = server.baseUrl();
            final CrawlResult result = new ConcurrentCrawler().crawl(new CrawlerConfiguration.Concurrent(10, URI.create(root + "/start"), 1000,
                    Duration.ofSeconds(2), Duration.ofSeconds(2), 5));
            assertThat(result.outcomes()).containsExactlyInAnyOrder(
                    new CrawlResult.Redirect(URI.create(root + "/start"), 302, URI.create(root + "/next")),
                    new CrawlResult.Response(URI.create(root + "/next"), 301));
            assertThat(result.diagnostics()).singleElement().asString().contains("Missing redirect location");
            server.verify(1, getRequestedFor(urlPathEqualTo("/next")));
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 100})
    void checksDistinctUrlsAndTerminatesOnCycle(final int maxLinks) throws Exception {
        withServer(server -> {
            final String root = server.baseUrl();
            final CrawlResult result = new ConcurrentCrawler().crawl(new CrawlerConfiguration.Concurrent(maxLinks, URI.create(root + "/demo/"), 1000,
                    Duration.ofSeconds(2), Duration.ofSeconds(2), 5));
            assertThat(result.outcomes()).containsExactlyInAnyOrder(
                    new CrawlResult.Response(URI.create(root + "/demo/"), 200),
                    new CrawlResult.Response(URI.create(root + "/demo/about"), 200),
                    new CrawlResult.Response(URI.create(root + "/demo/missing"), 404));
            assertThat(result.limitReached()).isFalse();
            server.verify(1, getRequestedFor(urlPathEqualTo("/demo/")));
            server.verify(1, getRequestedFor(urlPathEqualTo("/demo/about")));
            server.verify(1, getRequestedFor(urlPathEqualTo("/demo/missing")));
        });
    }

    @Test
    void checksExpandedFixtureWithoutExploringOutsideScope() throws Exception {
        withServer("expanded", server -> {
            final CrawlResult result = new ConcurrentCrawler().crawl(new CrawlerConfiguration.Concurrent(100, URI.create(server.baseUrl() + "/demo/"), 1000,
                    Duration.ofSeconds(2), Duration.ofSeconds(2), 5));
            assertThat(result.outcomes()).hasSize(9);
            assertThat(result.limitReached()).isFalse();
            server.verify(1, getRequestedFor(urlPathEqualTo("/demo/page1")));
            server.verify(1, getRequestedFor(urlPathEqualTo("/outside")));
            server.verify(0, getRequestedFor(urlPathEqualTo("/not-followed")));
        });
    }

    @Test
    void reportsWhenDiscoveryExceedsLimit() throws Exception {
        withServer(server -> {
            server.stubFor(get(urlPathEqualTo("/demo/"))
                    .willReturn(aResponse().withHeader("Content-Type", "text/html")
                            .withBody("<a href='/demo/'>Self</a><a href='/demo/new'>New</a>")));
            final CrawlResult result = new ConcurrentCrawler().crawl(new CrawlerConfiguration.Concurrent(1, URI.create(server.baseUrl() + "/demo/"), 1000,
                    Duration.ofSeconds(2), Duration.ofSeconds(2), 5));
            assertThat(result.outcomes()).hasSize(1);
            assertThat(result.limitReached()).isTrue();
            server.verify(0, getRequestedFor(urlPathEqualTo("/demo/new")));
        });
    }
}
