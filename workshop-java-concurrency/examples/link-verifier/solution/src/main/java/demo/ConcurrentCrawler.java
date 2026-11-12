package demo;

import module com.google.common;
import module java.base;
import module java.net.http;
import module org.jsoup;

import java.time.Duration;

import static demo.HttpResponses.isRedirect;

/**
 * Coordinates discovery on the calling thread; workers check one URL each.
 */
final class ConcurrentCrawler {

    private static final String USER_AGENT =
            "WorkshopLinkVerifier/1.0 (https://github.com/albertattard/workshop-java-concurrency)";

    CrawlResult crawl(final CrawlerConfiguration.Concurrent configuration) throws InterruptedException {
        final URI start = configuration.start();
        final int workers = configuration.workers();
        final CrawlScope scope = new CrawlScope(start);
        final CrawlState state = new CrawlState(start, configuration.maxLinks());
        final CrawlResult.Builder result = CrawlResult.builder();
        final OriginRateLimiters limiters = new OriginRateLimiters(configuration.requestsPerSecond());
        final ExecutorService executor = Executors.newFixedThreadPool(workers);

        try (HttpClient client = HttpClient.newBuilder().connectTimeout(configuration.connectTimeout()).build()) {
            try {
                final CompletionService<Checked> completed = new ExecutorCompletionService<>(executor);

                // Count submitted checks whose results have not yet been collected. This bounds
                // submissions to the worker count and keeps the crawl alive while a check may
                // still discover links, even when the pending queue is temporarily empty.
                int inFlight = 0;

                while (state.hasPending() || inFlight > 0) {
                    while (state.hasPending() && inFlight < workers) {
                        final URI url = state.removeNext();
                        final RateLimiter limiter = limiters.forUrl(url);
                        completed.submit(() -> check(client, url, scope, limiter, configuration.requestTimeout()));
                        inFlight++;
                    }

                    final Checked checked;
                    try {
                        final Future<Checked> future = completed.take();
                        checked = future.get();
                    } catch (final ExecutionException e) {
                        throw new IllegalStateException("Worker failed", e.getCause());
                    }
                    inFlight--;

                    result.outcome(checked.outcome());
                    result.diagnostics(checked.diagnostics());
                    switch (checked.outcome()) {
                        case CrawlResult.Redirect redirect -> {
                            if (state.offer(redirect.target()) == CrawlState.Admission.LIMIT_REACHED) {
                                result.limitReached();
                            }
                        }
                        case CrawlResult.Response _ -> {
                            for (final URI link : checked.links()) {
                                if (state.offer(link) == CrawlState.Admission.LIMIT_REACHED) {
                                    result.limitReached();
                                    break;
                                }
                            }
                        }
                        case CrawlResult.Failure _ -> {}
                    }
                }

                return result.build();
            } finally {
                executor.shutdownNow();
                boolean interrupted = false;
                while (!executor.isTerminated()) {
                    try {
                        executor.awaitTermination(1, TimeUnit.DAYS);
                    } catch (final InterruptedException e) {
                        interrupted = true;
                        executor.shutdownNow();
                    }
                }

                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private Checked check(final HttpClient client,
                          final URI url,
                          final CrawlScope scope,
                          final RateLimiter limiter,
                          final Duration timeout) throws InterruptedException {
        final List<URI> links = new ArrayList<>();
        final List<String> diagnostics = new ArrayList<>();
        final HttpRequest request = HttpRequest.newBuilder(url).timeout(timeout)
                .header("User-Agent", USER_AGENT).GET().build();

        try {
            if (Thread.interrupted()) {
                throw new InterruptedException("Crawl interrupted before waiting for a request permit");
            }

            while (!limiter.tryAcquire(100, TimeUnit.MILLISECONDS)) {
                if (Thread.interrupted()) {
                    throw new InterruptedException("Crawl interrupted while waiting for a request permit");
                }
            }

            if (Thread.interrupted()) {
                throw new InterruptedException("Crawl interrupted while waiting for a request permit");
            }

            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (isRedirect(response)) {
                final String location = response.headers().firstValue("Location").orElse("");
                if (location.isBlank()) {
                    diagnostics.add("Missing redirect location: " + url);
                } else {
                    try {
                        final URI target = Links.resource(url.resolve(location));
                        if (Links.isNotHttp(target)) {
                            diagnostics.add("Unsupported redirect location on " + url + ": " + location);
                        } else {
                            return Checked.redirect(url, response.statusCode(), target, links, diagnostics);
                        }
                    } catch (final IllegalArgumentException e) {
                        diagnostics.add("Invalid redirect location on " + url + ": " + location);
                    }
                }
            } else if (shouldExtractLinks(url, response, scope)) {
                for (final var element : Jsoup.parse(response.body(), url.toString()).select("a[href]")) {
                    try {
                        final URI link = Links.resource(url.resolve(element.attr("href")));
                        if (!Links.isNotHttp(link)) {
                            links.add(link);
                        }
                    } catch (final IllegalArgumentException e) {
                        diagnostics.add("Invalid link on " + url + ": " + element.attr("href"));
                    }
                }
            }

            return Checked.response(url, response.statusCode(), links, diagnostics);
        } catch (final IOException e) {
            return Checked.failure(url, e.toString(), links, diagnostics);
        }
    }

    private static boolean shouldExtractLinks(final URI url, final HttpResponse<String> response, final CrawlScope scope) {
        return response.statusCode() >= 200
                && response.statusCode() < 300
                && scope.contains(url)
                && response.headers().firstValue("Content-Type").orElse("")
                .toLowerCase(Locale.ROOT)
                .startsWith("text/html");
    }

    private record Checked(CrawlResult.Outcome outcome, List<URI> links, List<String> diagnostics) {

        private static Checked response(final URI url, final int status, final List<URI> links, final List<String> diagnostics) {
            return new Checked(new CrawlResult.Response(url, status), links, diagnostics);
        }

        private static Checked redirect(final URI url, final int status, final URI target, final List<URI> links, final List<String> diagnostics) {
            return new Checked(new CrawlResult.Redirect(url, status, target), links, diagnostics);
        }

        private static Checked failure(final URI url, final String message, final List<URI> links, final List<String> diagnostics) {
            return new Checked(new CrawlResult.Failure(url, message), links, diagnostics);
        }
    }
}
