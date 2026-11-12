package demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static demo.WireMockTestSupport.withServer;
import static org.assertj.core.api.Assertions.assertThat;

class MainTest {

    @Test
    @Timeout(10)
    void checksFixtureOncePerUrlAndTerminatesOnCycle() throws Exception {
        withServer(server -> {
            final StringWriter output = new StringWriter();
            final StringWriter errors = new StringWriter();
            final int exitCode = new CommandLine(new LinkVerifierCommand())
                    .setOut(new PrintWriter(output))
                    .setErr(new PrintWriter(errors))
                    .execute(server.baseUrl() + "/demo/");

            assertThat(exitCode).isZero();
            assertThat(errors.toString()).isEmpty();
            assertThat(output.toString()).contains("200 " + server.baseUrl() + "/demo/",
                    "404 " + server.baseUrl() + "/demo/missing");
            server.verify(1, getRequestedFor(urlPathEqualTo("/demo/")));
            server.verify(1, getRequestedFor(urlPathEqualTo("/demo/about")));
            server.verify(1, getRequestedFor(urlPathEqualTo("/demo/missing")));
        });
    }
}
