package demo.chat.spring;

import org.springframework.ai.document.Document;

import java.util.Optional;
import java.util.OptionalDouble;

public final class DocumentUtils {

    public static OptionalDouble score(final Document document) {
        return Optional.ofNullable(document.getScore()).stream()
                .mapToDouble(Double::doubleValue)
                .findFirst();
    }

    public static int compare(final Document a, final Document b) {
        return Double.compare(score(b).orElse(0), score(a).orElse(0));
    }

    public static Optional<String> title(final Document document) {
        return metadata(document, "title");
    }

    public static Optional<String> subtitle(final Document document) {
        return metadata(document, "subtitle");
    }

    public static Optional<String> slug(final Document document) {
        return metadata(document, "slug");
    }

    public static Optional<String> path(final Document document) {
        return metadata(document, "path");
    }

    private static Optional<String> metadata(final Document document, final String key) {
        return document.getMetadata().get(key) instanceof final String value
                ? Optional.of(value)
                : Optional.empty();
    }

    private DocumentUtils() {}
}
