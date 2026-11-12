package demo;

public class Main {

    public static void main(final String[] args) {
        System.out.println("Before request: bound=" + RequestContext.isBound());
        RequestContext.runWith("request-42", Main::service);
        System.out.println("After request: bound=" + RequestContext.isBound());
    }

    private static void service() {
        repository("Service entry");

        RequestContext.runWith("nested-request", () -> repository("Nested scope"));
        repository("After nested scope");

        try {
            RequestContext.runWith("failing-request", () -> {
                repository("Failing scope");
                throw new IllegalStateException("demonstration failure");
            });
        } catch (final IllegalStateException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        repository("After failing scope");
    }

    private static void repository(final String stage) {
        System.out.println(stage + ": requestId=" + RequestContext.requestId());
    }
}
