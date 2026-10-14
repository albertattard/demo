package demo;

import java.util.List;

public record RetrievalQueries(boolean supported, List<String> queries) {
    public RetrievalQueries {
        queries = List.copyOf(queries);
    }
}
