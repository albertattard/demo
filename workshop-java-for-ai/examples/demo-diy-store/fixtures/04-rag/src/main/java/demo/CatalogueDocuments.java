package demo;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CatalogueDocuments {

    public List<RetrievedProduct> mapProducts(final List<Document> documents) {
        return documents.stream()
                .map(CatalogueDocuments::mapProduct)
                .toList();
    }

    private static RetrievedProduct mapProduct(final Document document) {
        if (document.getMetadata().get("id") instanceof Number productId) {
            return new RetrievedProduct(
                    productId.longValue(),
                    document.getText(),
                    Optional.ofNullable(document.getScore()).orElse(0.0D));
        }

        throw new IllegalStateException("A catalogue document is missing its numeric product ID.");
    }

    public List<Document> mapDocuments(final List<Product> products) {
        return products.stream()
                .map(CatalogueDocuments::mapDocument)
                .toList();
    }

    private static Document mapDocument(final Product product) {
        return Document.builder()
                .text(mapText(product))
                .metadata(Map.of("id", product.id()))
                .build();
    }

    private static String mapText(final Product product) {
        return "ID: %d%nName: %s%nDescription: %s%nKeywords: %s".formatted(
                product.id(), product.name(), product.description(), product.keywords());
    }
}
