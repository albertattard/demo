package demo.chat;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.SequencedSet;

import static java.util.Objects.requireNonNull;

public sealed interface DialogueMessage {

    String text();

    static DialogueMessage user(final String text) {
        return new User(text);
    }

    static DialogueMessage assistant(final String text) {
        return new Assistant(text);
    }

    static DialogueMessage assistant(final String text, final SequencedSet<AssistantReference> cards) {
        return new Assistant(text, cards);
    }

    record User(String text) implements DialogueMessage {}

    record Assistant(String text, SequencedSet<AssistantReference> cards) implements DialogueMessage {

        public Assistant {
            requireNonNull(text);
            cards = Collections.unmodifiableSequencedSet(new LinkedHashSet<>(cards));
        }

        public Assistant(String text) {
            this(text, new LinkedHashSet<>());
        }
    }
}
