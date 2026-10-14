package demo;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

@Service
class ModelGateway {

    private final ChatClient client;
    private final Tracer tracer;

    ModelGateway(final ChatClient.Builder builder, final Tracer tracer) {
        this.client = builder.build();
        this.tracer = tracer;
    }

    Assistant prompt(final Prompt prompt) {
        requireNonNull(prompt);

        final Span span = tracer.nextSpan().name("model.gateway.prompt");
        try (Tracer.SpanInScope _ = tracer.withSpan(span.start())) {
            span.tag("llm.prompt", prompt.prompt());

            final String assistant = client.prompt(prompt.prompt())
                    .call()
                    .content()
                    .trim();

            span.tag("llm.response", assistant);
            span.tag("llm.response.length", String.valueOf(assistant.length()));

            return new Assistant(assistant);
        } finally {
            span.end();
        }
    }
}
