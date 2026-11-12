package demo;

import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.net.URI;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static demo.WireMockTestSupport.withServer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SequentialCrawlerTest {

    @Test
    @Timeout(10)
    void propagatesInterruptionWithoutSendingARequest() throws Exception {
        withServer(server -> {
            Thread.currentThread().interrupt();
            try {
                assertThatThrownBy(() -> runCrawler(server.baseUrl() + "/demo/"))
                        .isInstanceOf(InterruptedException.class);
                server.verify(0, getRequestedFor(urlPathEqualTo("/demo/")));
            } finally {
                Thread.interrupted();
            }
        });
    }

    @Test
    @Timeout(10)
    void returnsRequestFailures() throws Exception {
        withServer(server -> {
            server.stubFor(get(urlPathEqualTo("/failure"))
                    .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

            final CrawlResult result = runCrawler(server.baseUrl() + "/failure");

            assertThat(result.outcomes()).singleElement().isInstanceOf(CrawlResult.Failure.class);
            assertThat(result.limitReached()).isFalse();
        });
    }

    @Test
    @Timeout(10)
    void countsStartingPageTowardsConfiguredLimit() throws Exception {
        withServer(server -> {
            final CrawlResult result = runCrawler(server.baseUrl() + "/demo/", "1");
            assertThat(result.limitReached()).isTrue();
            assertThat(result.outcomes()).containsExactly(
                    new CrawlResult.Response(URI.create(server.baseUrl() + "/demo/"), 200));
            server.verify(1, getRequestedFor(urlPathEqualTo("/demo/"))
                    .withHeader("User-Agent", equalTo("WorkshopLinkVerifier/1.0 (https://github.com/albertattard/workshop-java-concurrency)")));
            server.verify(0, getRequestedFor(urlPathEqualTo("/demo/about")));
            server.verify(0, getRequestedFor(urlPathEqualTo("/demo/missing")));
        });
    }

    @Test
    @Timeout(10)
    void followsRelativeRedirectsAndStopsOnCycle() throws Exception {
        withServer(server -> {
            server.stubFor(get(urlPathEqualTo("/chain/start"))
                    .willReturn(aResponse().withStatus(301).withHeader("Location", "next")));
            server.stubFor(get(urlPathEqualTo("/chain/next"))
                    .willReturn(aResponse().withStatus(302).withHeader("Location", "/chain/start")));

            final CrawlResult result = runCrawler(server.baseUrl() + "/chain/start");
            assertThat(result.limitReached()).isFalse();
            assertThat(result.outcomes()).containsExactly(
                    new CrawlResult.Redirect(URI.create(server.baseUrl() + "/chain/start"), 301,
                            URI.create(server.baseUrl() + "/chain/next")),
                    new CrawlResult.Redirect(URI.create(server.baseUrl() + "/chain/next"), 302,
                            URI.create(server.baseUrl() + "/chain/start")));

            server.verify(1, getRequestedFor(urlPathEqualTo("/chain/start")));
            server.verify(1, getRequestedFor(urlPathEqualTo("/chain/next")));
        });
    }

    @Test
    @Timeout(10)
    void checksRedirectOutsideScopeWithoutExploringItsLinks() throws Exception {
        withServer(server -> {
            server.stubFor(get(urlPathEqualTo("/start"))
                    .willReturn(aResponse().withStatus(308).withHeader("Location", "/outside")));
            server.stubFor(get(urlPathEqualTo("/outside"))
                    .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/html")
                            .withBody("<a href='/not-followed'>link</a>")));

            runCrawler(server.baseUrl() + "/start");

            server.verify(1, getRequestedFor(urlPathEqualTo("/outside")));
            server.verify(0, getRequestedFor(urlPathEqualTo("/not-followed")));
        });
    }

    @Test
    @Timeout(10)
    void countsRedirectTargetsTowardsLimit() throws Exception {
        withServer(server -> {
            server.stubFor(get(urlPathEqualTo("/start"))
                    .willReturn(aResponse().withStatus(303).withHeader("Location", "/next")));

            runCrawler(server.baseUrl() + "/start", "1");

            server.verify(1, getRequestedFor(urlPathEqualTo("/start")));
            server.verify(0, getRequestedFor(urlPathEqualTo("/next")));
        });
    }

    @Test
    @Timeout(10)
    void handlesMissingInvalidAndUnsupportedLocations() throws Exception {
        withServer(server -> {
            server.stubFor(get(urlPathEqualTo("/missing-location")).willReturn(aResponse().withStatus(301)));
            server.stubFor(get(urlPathEqualTo("/invalid-location"))
                    .willReturn(aResponse().withStatus(302).withHeader("Location", "http://[invalid")));
            server.stubFor(get(urlPathEqualTo("/unsupported-location"))
                    .willReturn(aResponse().withStatus(307).withHeader("Location", "mailto:test@example.com")));

            final CrawlResult missing = runCrawler(server.baseUrl() + "/missing-location");
            final CrawlResult invalid = runCrawler(server.baseUrl() + "/invalid-location");
            final CrawlResult unsupported = runCrawler(server.baseUrl() + "/unsupported-location");
            assertThat(missing.outcomes()).containsExactly(new CrawlResult.Response(URI.create(server.baseUrl() + "/missing-location"), 301));
            assertThat(invalid.outcomes()).containsExactly(new CrawlResult.Response(URI.create(server.baseUrl() + "/invalid-location"), 302));
            assertThat(unsupported.outcomes()).containsExactly(new CrawlResult.Response(URI.create(server.baseUrl() + "/unsupported-location"), 307));
            assertThat(missing.diagnostics()).singleElement().asString().contains("Missing redirect location");
            assertThat(invalid.diagnostics()).singleElement().asString().contains("Invalid redirect location");
            assertThat(unsupported.diagnostics()).singleElement().asString().contains("Unsupported redirect location");

            server.verify(1, getRequestedFor(urlPathEqualTo("/missing-location")));
            server.verify(1, getRequestedFor(urlPathEqualTo("/invalid-location")));
            server.verify(1, getRequestedFor(urlPathEqualTo("/unsupported-location")));
        });
    }

    private static CrawlResult runCrawler(final String... args) throws InterruptedException {
        final int maxLinks = args.length == 2 ? Integer.parseInt(args[1]) : 100;
        return new SequentialCrawler().crawl(new CrawlerConfiguration.Sequential(
                maxLinks, URI.create(args[0]), 5.0,
                Duration.ofSeconds(5), Duration.ofSeconds(10)));
    }
}
