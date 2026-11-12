package demo;

import module java.base;
import module java.net.http;
import module org.jsoup;

import static demo.HttpResponses.isRedirect;

final class SequentialCrawler {

    private static final String USER_AGENT =
            "WorkshopLinkVerifier/1.0 (https://github.com/albertattard/workshop-java-concurrency)";

    CrawlResult crawl(final CrawlerConfiguration.Sequential configuration) throws InterruptedException {
        final URI start = configuration.start();
        final CrawlScope scope = new CrawlScope(start);
        final CrawlState state = new CrawlState(start, configuration.maxLinks());
        final CrawlResult.Builder result = CrawlResult.builder();
        final OriginRateLimiters limiters = new OriginRateLimiters(configuration.requestsPerSecond());

        try (HttpClient client = HttpClient.newBuilder().connectTimeout(configuration.connectTimeout()).build()) {
            while (state.hasPending()) {
                final URI url = state.removeNext();
                final HttpRequest request = HttpRequest.newBuilder(url)
                        .timeout(configuration.requestTimeout())
                        .header("User-Agent", USER_AGENT)
                        .GET()
                        .build();
                try {
                    if (Thread.interrupted()) {
                        throw new InterruptedException("Crawl interrupted before waiting for a request permit");
                    }

                    // Guava allows bursts after inactivity; this is not strict request spacing.
                    // Its wait is uninterruptible, so propagate interruption once it finishes.
                    limiters.forUrl(url).acquire();
                    if (Thread.interrupted()) {
                        throw new InterruptedException("Crawl interrupted while waiting for a request permit");
                    }

                    final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (isRedirect(response)) {
                        final String location = response.headers().firstValue("Location").orElse("");
                        if (location.isBlank()) {
                            result.diagnostic("Missing redirect location: " + url);
                        } else {
                            try {
                                final URI target = Links.resource(url.resolve(location));
                                if (Links.isNotHttp(target)) {
                                    result.diagnostic("Unsupported redirect location on " + url + ": " + location);
                                } else {
                                    result.redirect(url, response.statusCode(), target);
                                    if (state.offer(target) == CrawlState.Admission.LIMIT_REACHED) {
                                        result.limitReached();
                                    }
                                    continue;
                                }
                            } catch (final IllegalArgumentException e) {
                                result.diagnostic("Invalid redirect location on " + url + ": " + location);
                            }
                        }
                        result.response(url, response.statusCode());
                        continue;
                    }

                    result.response(url, response.statusCode());
                    if (!shouldExtractLinks(url, response, scope)) {
                        continue;
                    }

                    for (final var element : Jsoup.parse(response.body(), url.toString()).select("a[href]")) {
                        try {
                            final URI link = Links.resource(url.resolve(element.attr("href")));
                            if (Links.isNotHttp(link)) {
                                continue;
                            }

                            if (state.offer(link) == CrawlState.Admission.LIMIT_REACHED) {
                                result.limitReached();
                            }
                        } catch (final IllegalArgumentException e) {
                            result.diagnostic("Invalid link on " + url + ": " + element.attr("href"));
                        }
                    }
                } catch (final IOException e) {
                    result.failure(url, e.toString());
                }
            }
        }

        return result.build();
    }

    private static boolean shouldExtractLinks(
            final URI url,
            final HttpResponse<?> response,
            final CrawlScope scope) {
        return response.statusCode() >= 200
                && response.statusCode() < 300
                && scope.contains(url)
                && response.headers().firstValue("Content-Type").orElse("")
                .toLowerCase(Locale.ROOT)
                .startsWith("text/html");
    }
}
