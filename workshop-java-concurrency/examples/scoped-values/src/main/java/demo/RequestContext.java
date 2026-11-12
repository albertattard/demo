package demo;

final class RequestContext {

    private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

    private RequestContext() {}

    static void runWith(final String requestId, final Runnable action) {
        ScopedValue.where(REQUEST_ID, requestId).run(action);
    }

    static String requestId() {
        return REQUEST_ID.get();
    }

    static boolean isBound() {
        return REQUEST_ID.isBound();
    }
}
