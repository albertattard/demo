package demo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    private final ProductRepository repository = mock(ProductRepository.class);
    private final ModelGateway modelGateway = mock(ModelGateway.class);
    private final ProductService service = new ProductService(repository, modelGateway);

    @Test
    void findsAndDeduplicatesProductsForTheRecommendedCategories() {
        final Product paint = new Product(1, "Interior matt paint", "paint, wall", "Durable paint for interior walls.");
        final Product brush = new Product(2, "Paint brush", "paint, brush", "A brush for interior paint.");
        when(modelGateway.recommend("I would like to paint my living room."))
                .thenReturn(new RecommendationBrief(true, List.of("paint", "brush")));
        when(repository.search("paint")).thenReturn(List.of(paint, brush));
        when(repository.search("brush")).thenReturn(List.of(brush));

        final List<Product> products = service.recommendProducts("I would like to paint my living room.");

        assertEquals(List.of(paint, brush), products);
        verify(repository).search("paint");
        verify(repository).search("brush");
    }
}
