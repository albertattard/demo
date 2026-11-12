package demo;

import module com.google.common;
import module java.base;
import module java.net.http;
import module org.jsoup;

/**
 * Coordinates discovery on the calling thread; workers check one URL each.
 */
final class ConcurrentCrawler {

    private static final String USER_AGENT =
            "WorkshopLinkVerifier/1.0 (https://github.com/albertattard/workshop-java-concurrency)";

    CrawlResult crawl(final CrawlerConfiguration.Concurrent configuration) throws InterruptedException {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
