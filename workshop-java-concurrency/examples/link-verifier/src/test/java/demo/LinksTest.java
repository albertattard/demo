package demo;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class LinksTest {

    @Test
    void removesFragmentsButPreservesQueries() {
        assertThat(Links.resource(URI.create("https://example.com/a/../demo?q=1#part")))
                .isEqualTo(URI.create("https://example.com/demo?q=1"));
    }
}
