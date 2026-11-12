package demo;

import java.net.URI;

final class Links {

    static URI resource(final URI uri) {
        final String value = uri.toString();
        final int fragment = value.indexOf('#');
        return URI.create(fragment < 0 ? value : value.substring(0, fragment)).normalize();
    }

    static boolean isNotHttp(final URI uri) {
        return uri.getHost() == null
                || (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()));
    }

    static int port(final URI uri) {
        return uri.getPort() >= 0
                ? uri.getPort()
                : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);
    }

    private Links() {}
}
