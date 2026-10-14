package demo.chat;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.SequencedSet;

import static java.util.Objects.requireNonNull;

public record AssistantResponse(String text, SequencedSet<AssistantReference> references) {
    public AssistantResponse {
        requireNonNull(text);
        references = Collections.unmodifiableSequencedSet(new LinkedHashSet<>(references));
    }

    public AssistantResponse(final String text) {
        this(text, new LinkedHashSet<>());
    }
}
