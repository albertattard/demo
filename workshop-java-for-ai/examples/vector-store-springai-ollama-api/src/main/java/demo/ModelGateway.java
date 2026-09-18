package demo;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Service
final class ModelGateway {

    private final VectorStore vectorStore;

    private ModelGateway(final VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    void add(final String text, final Map<String, Object> metadata) {
        requireNonNull(text);

        final Document document = Document.builder()
                .metadata(metadata)
                .text(text)
                .build();

        vectorStore.add(List.of(document));
    }

    List<Result> search(final String query) {
        requireNonNull(query);

        final SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(5)
                .build();

        return similaritySearch(searchRequest);
    }

    List<Result> findByMetadataSlug(final String slug) {
        requireNonNull(slug);

        final SearchRequest searchRequest = SearchRequest.builder()
                .filterExpression(new Filter.Expression(Filter.ExpressionType.EQ, new Filter.Key("slug"), new Filter.Value(slug)))
                .topK(5)
                .build();

        return similaritySearch(searchRequest);
    }

    private List<Result> similaritySearch(final SearchRequest searchRequest) {
        return vectorStore.similaritySearch(searchRequest)
                .stream()
                .map(Result::of)
                .toList();
    }

    record Result(String id,
                  String text,
                  Map<String, Object> metadata,
                  double score) implements Comparable<Result> {

        /* Higher score comes first */
        private static final Comparator<Result> NATURAL_ORDER = Comparator.comparing(Result::score).reversed();

        Result {
            metadata = Map.copyOf(metadata);
        }

        static Result of(final Document document) {
            final String id = document.getId();
            final String text = document.getText();
            final Map<String, Object> metadata = document.getMetadata();
            final double score = Optional.ofNullable(document.getScore()).orElse(0.0D);

            return new Result(id, text, metadata, score);
        }

        String slug() {
            return metadata.get("slug") instanceof String slug
                    ? slug
                    : null;
        }

        @Override
        public int compareTo(final Result other) {
            return NATURAL_ORDER.compare(this, other);
        }
    }
}
