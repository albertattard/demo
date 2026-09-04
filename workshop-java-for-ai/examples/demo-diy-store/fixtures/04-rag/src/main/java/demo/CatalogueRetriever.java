package demo;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogueRetriever {

    private static final int RETRIEVAL_LIMIT = 10;

    private final VectorStore vectorStore;
    private final CatalogueDocuments catalogueDocuments;

    public CatalogueRetriever(
            final VectorStore vectorStore,
            final CatalogueDocuments catalogueDocuments) {
        this.vectorStore = vectorStore;
        this.catalogueDocuments = catalogueDocuments;
    }

    public List<RetrievedProduct> retrieve(final String query) {
        final SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(RETRIEVAL_LIMIT)
                .build();
        final List<Document> documents = vectorStore.similaritySearch(request);
        return catalogueDocuments.mapProducts(documents);
    }
}
