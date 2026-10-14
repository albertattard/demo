package demo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocalStoreToolsTest {
    private final CatalogueRetriever catalogueRetriever = mock(CatalogueRetriever.class);
    private final ProductRepository repository = mock(ProductRepository.class);
    private final LocalStoreTools tools = new LocalStoreTools(
            catalogueRetriever, repository);

    @Test
    void returnsVectorMatchesForWordingThatDoesNotNameTheProduct() {
        final Product paint = new Product(1, "Interior matt paint", "paint, wall", "Paint for walls.");
        when(catalogueRetriever.retrieve("refresh the lounge walls"))
                .thenReturn(List.of(new RetrievedProduct(1, "Interior matt paint", 0.87D)));
        when(repository.findByIds(List.of(1L))).thenReturn(List.of(paint));

        assertEquals(List.of(paint), tools.searchCatalogue("refresh the lounge walls"));
    }

}
