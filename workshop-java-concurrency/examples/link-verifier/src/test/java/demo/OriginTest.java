package demo;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class OriginTest {

    @Test
    void ignoresCasePathsQueriesAndFragments() {
        assertThat(origin("HTTP://EXAMPLE.COM/demo?a=1#section"))
                .isEqualTo(origin("http://example.com/other"));
    }

    @Test
    void treatsExplicitAndImplicitDefaultPortsAsTheSameOrigin() {
        assertThat(origin("http://example.com/"))
                .isEqualTo(origin("http://example.com:80/"));
        assertThat(origin("https://example.com/"))
                .isEqualTo(origin("https://example.com:443/"));
    }

    @Test
    void distinguishesSchemesHostsAndNonDefaultPorts() {
        assertThat(origin("http://example.com/"))
                .isNotEqualTo(origin("https://example.com/"))
                .isNotEqualTo(origin("http://other.example/"))
                .isNotEqualTo(origin("http://example.com:8080/"));
    }

    private static Origin origin(final String url) {
        return Origin.of(URI.create(url));
    }
}
