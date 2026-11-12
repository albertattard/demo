package demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(5)
class RequestContextTest {

    @Test
    void bindsForTheCallAndBecomesUnboundOnReturn() {
        assertUnbound();

        RequestContext.runWith("request-42", () -> {
            assertThat(RequestContext.isBound()).isTrue();
            assertThat(RequestContext.requestId()).isEqualTo("request-42");
        });

        assertUnbound();
    }

    @Test
    void restoresTheOuterBindingAfterNestedCompletion() {
        RequestContext.runWith("outer", () -> {
            RequestContext.runWith("inner", () ->
                    assertThat(RequestContext.requestId()).isEqualTo("inner"));

            assertThat(RequestContext.requestId()).isEqualTo("outer");
        });

        assertUnbound();
    }

    @Test
    void restoresTheOuterBindingAndPropagatesTheOriginalException() {
        final IllegalStateException failure = new IllegalStateException("failed request");

        RequestContext.runWith("outer", () -> {
            assertThatThrownBy(() -> RequestContext.runWith("inner", () -> {
                assertThat(RequestContext.requestId()).isEqualTo("inner");
                throw failure;
            })).isSameAs(failure);

            assertThat(RequestContext.requestId()).isEqualTo("outer");
        });

        assertUnbound();
    }

    @Test
    void becomesUnboundWhenTheOuterCallFails() {
        final IllegalStateException failure = new IllegalStateException("failed request");

        assertThatThrownBy(() -> RequestContext.runWith("request-42", () -> {
            throw failure;
        })).isSameAs(failure);

        assertUnbound();
    }

    @Test
    void doesNotLeakContextBetweenSequentialRequests() {
        RequestContext.runWith("first", () ->
                assertThat(RequestContext.requestId()).isEqualTo("first"));
        assertUnbound();

        RequestContext.runWith("second", () ->
                assertThat(RequestContext.requestId()).isEqualTo("second"));
        assertUnbound();
    }

    private static void assertUnbound() {
        assertThat(RequestContext.isBound()).isFalse();
        assertThatThrownBy(RequestContext::requestId).isInstanceOf(NoSuchElementException.class);
    }
}
