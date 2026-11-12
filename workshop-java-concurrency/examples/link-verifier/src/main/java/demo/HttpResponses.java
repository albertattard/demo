package demo;

import java.net.http.HttpResponse;

class HttpResponses {

    static boolean isRedirect(final HttpResponse<?> response) {
        return isRedirect(response.statusCode());
    }

    static boolean isRedirect(final int status) {
        return status == 301
                || status == 302
                || status == 303
                || status == 307
                || status == 308;
    }

    private HttpResponses() {}
}
