package demo;

import java.util.Arrays;
import java.util.Optional;

public enum Language {
    ITALIAN("Italian"),
    GERMAN("German"),
    ;

    private final String caption;

    static Optional<Language> of(final String text) {
        return Arrays.stream(values())
                .filter(e -> e.caption.equalsIgnoreCase(text))
                .findFirst();
    }

    Language(final String caption) {this.caption = caption;}

    @Override
    public String toString() {
        return caption;
    }
}
