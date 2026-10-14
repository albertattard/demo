package demo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    private final ProductRepository repository = mock(ProductRepository.class);
    private final ModelGateway modelGateway = mock(ModelGateway.class);
    private final ImageResizer imageResizer = mock(ImageResizer.class);
    private final CatalogueRetriever catalogueRetriever = mock(CatalogueRetriever.class);
    private final ProductService service = new ProductService(repository, modelGateway, imageResizer, catalogueRetriever, false);

    @Test
    void fetchesOnlyDatabaseProductsWhoseIdsWereReturnedFromRetrievedDocuments() {
        final RetrievedProduct paint = product(1, "Interior matt paint");
        final RetrievedProduct roller = product(2, "Paint roller set");
        final Product expected = new Product(1, "Interior matt paint", "paint, wall", "Paint for walls.");
        when(modelGateway.createRetrievalQueries("Paint my living room"))
                .thenReturn(new RetrievalQueries(true, List.of("wall paint", "rollers")));
        when(catalogueRetriever.retrieve("wall paint")).thenReturn(List.of(paint));
        when(catalogueRetriever.retrieve("rollers")).thenReturn(List.of(paint, roller));
        when(modelGateway.selectRetrievedProducts(eq("Paint my living room"), any()))
                .thenReturn(new ProductRecommendation(List.of(1L)));
        when(repository.findByIds(List.of(1L))).thenReturn(List.of(expected));

        final List<Product> products = service.recommendProducts("Paint my living room");

        assertEquals(List.of(expected), products);
        verify(modelGateway).selectRetrievedProducts("Paint my living room", List.of(paint, roller));
    }

    @Test
    void rejectsAProductIdThatWasNotReturnedByTheVectorStore() {
        when(modelGateway.createRetrievalQueries(any())).thenReturn(new RetrievalQueries(true, List.of("wall paint")));
        when(catalogueRetriever.retrieve(any())).thenReturn(List.of(product(1, "Interior matt paint")));
        when(modelGateway.selectRetrievedProducts(any(), any())).thenReturn(new ProductRecommendation(List.of(999L)));

        assertTrue(service.recommendProducts("Paint a wall").isEmpty());
    }

    @Test
    void doesNotRetrieveProductsForAnUnsupportedRequest() {
        when(modelGateway.createRetrievalQueries("Write my tax return"))
                .thenReturn(new RetrievalQueries(false, List.of()));

        assertTrue(service.recommendProducts("Write my tax return").isEmpty());
        org.mockito.Mockito.verifyNoInteractions(catalogueRetriever, repository);
    }

    private static RetrievedProduct product(final long id, final String name) {
        return new RetrievedProduct(id, "ID: %d%nName: %s".formatted(id, name), 0.9D);
    }
}
