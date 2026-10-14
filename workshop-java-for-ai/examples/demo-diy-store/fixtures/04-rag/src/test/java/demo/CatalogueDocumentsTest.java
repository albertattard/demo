package demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogueDocumentsTest {
    @Test
    void convertsTheDatabaseProductIntoADocumentWithItsStableId() {
        final Product product = new Product(456, "Interior matt paint", "paint, wall", "Durable paint for walls.");

        final List<Document> documents = new CatalogueDocuments().mapDocuments(List.of(product));

        assertEquals(456L, documents.getFirst().getMetadata().get("id"));
        assertTrue(documents.getFirst().getText().contains("Interior matt paint"));
    }
}
