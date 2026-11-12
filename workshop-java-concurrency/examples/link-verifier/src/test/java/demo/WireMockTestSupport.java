package demo;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

class WireMockTestSupport {

    static void withServer(final ThrowingConsumer<WireMockServer> consumer) throws Exception {
        withServer("basic", consumer);
    }

    static void withServer(final String fixture, final ThrowingConsumer<WireMockServer> consumer) throws Exception {
        final WireMockServer server = new WireMockServer(wireMockConfig().dynamicPort().usingFilesUnderDirectory("fixtures/" + fixture));
        server.start();
        try {
            consumer.accept(server);
        } finally {
            server.stop();
        }
    }

    interface ThrowingConsumer<T> {
        void accept(T r) throws Exception;
    }

    private WireMockTestSupport() {}
}
