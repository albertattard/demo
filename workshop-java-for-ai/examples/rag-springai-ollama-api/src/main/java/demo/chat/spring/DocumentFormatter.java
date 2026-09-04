package demo.chat.spring;

import org.springframework.ai.document.Document;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DocumentFormatter implements Function<List<Document>, String> {

    private static final Function<List<Document>, String> INSTANCE = new DocumentFormatter();

    @Override
    public String apply(final List<Document> documents) {
        return Documents.of(documents)
                .map(DocumentFormatter::formatDocuments)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static String formatDocuments(final Documents documents) {
        return "[CHUNK START]"
               + "\nid=" + documents.path
               + "\ntext:"
               + documents.documents().stream()
                       .sorted(DocumentUtils::compare)
                       .map(Document::getText)
                       .filter(Objects::nonNull)
                       .map("\n"::concat)
                       .collect(Collectors.joining())
               + "\n[CHUNK END]";
    }

    private record Documents(String path, List<Document> documents, double maxScore) implements Comparable<Documents> {

        private static Stream<Documents> of(final List<Document> documents) {
            return groupByPath(documents).entrySet().stream()
                    .map(Documents::of)
                    .sorted();
        }

        private static Map<String, List<Document>> groupByPath(final List<Document> documents) {
            return documents.stream()
                    .flatMap(doc -> DocumentUtils.path(doc).map(p -> new AbstractMap.SimpleEntry<>(p, doc)).stream())
                    .collect(Collectors.groupingBy(
                            AbstractMap.SimpleEntry::getKey,
                            Collectors.collectingAndThen(
                                    Collectors.mapping(AbstractMap.SimpleEntry::getValue, Collectors.toCollection(ArrayList::new)),
                                    list -> {
                                        list.sort(Comparator.<Document>comparingDouble(d -> DocumentUtils.score(d).orElse(0.0)).reversed());
                                        return list;
                                    }
                            )
                    ));
        }

        private static Documents of(final Map.Entry<String, List<Document>> entry) {
            final String path = entry.getKey();
            final List<Document> documents = entry.getValue();
            final double maxScore = documents.stream()
                    .map(Document::getScore)
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0);

            return new Documents(path, documents, maxScore);
        }

        @Override
        public int compareTo(final Documents other) {
            return Double.compare(other.maxScore, maxScore);
        }
    }

    public static Function<List<Document>, String> instance() {
        return INSTANCE;
    }

    private DocumentFormatter() {}
}
