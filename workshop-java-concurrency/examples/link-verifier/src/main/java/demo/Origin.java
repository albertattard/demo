package demo;

import java.net.URI;
import java.util.Locale;

record Origin(String scheme, String host, int port) {

    static Origin of(final URI url) {
        return new Origin(url.getScheme().toLowerCase(Locale.ROOT),
                url.getHost().toLowerCase(Locale.ROOT), Links.port(url));
    }
}
