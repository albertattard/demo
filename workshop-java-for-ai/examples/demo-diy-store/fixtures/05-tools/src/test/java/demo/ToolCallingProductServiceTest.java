package demo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ToolCallingProductServiceTest {
    private final ProductRepository repository = mock(ProductRepository.class);
    private final ModelGateway modelGateway = mock(ModelGateway.class);
    private final CatalogueRetriever catalogueRetriever = mock(CatalogueRetriever.class);
    private final ProductService service = new ProductService(repository, modelGateway,
            mock(ImageResizer.class), catalogueRetriever, true);

    @Test
    void sendsOnlyTheCustomerQuestionToTheToolCallingModel() {
        final String question = "What can I collect today to paint my living room?";
        when(modelGateway.recommendProductsWithTools(question)).thenReturn(new ProductRecommendation(List.of()));

        service.recommendProducts(question);

        verify(modelGateway).recommendProductsWithTools(question);
    }

    @Test
    void rejectsAProductIdThatWasNotReturnedBySearchCatalogue() {
        when(modelGateway.recommendProductsWithTools("paint"))
                .thenReturn(new ProductRecommendation(List.of(999L)));
        when(catalogueRetriever.retrieve("paint"))
                .thenReturn(List.of(new RetrievedProduct(1, "Interior matt paint", 0.9D)));
        when(repository.findByIds(List.of(1L))).thenReturn(List.of(paint()));

        assertEquals(List.of(), service.recommendProducts("paint"));
    }

    @Test
    void returnsProductsSelectedByTheToolCallingModelAfterCatalogueValidation() {
        when(modelGateway.recommendProductsWithTools("paint"))
                .thenReturn(new ProductRecommendation(List.of(1L)));
        when(catalogueRetriever.retrieve("paint"))
                .thenReturn(List.of(new RetrievedProduct(1, "Interior matt paint", 0.9D)));
        when(repository.findByIds(List.of(1L))).thenReturn(List.of(paint()));

        assertEquals(List.of(paint()), service.recommendProducts("paint"));
    }

    private static Product paint() {
        return new Product(1, "Interior matt paint", "paint, wall", "Paint for walls.");
    }
}
