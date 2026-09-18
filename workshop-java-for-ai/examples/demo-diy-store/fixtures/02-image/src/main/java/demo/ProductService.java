package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    private static final int MAXIMUM_IMAGE_DIMENSION = 1024;

    private final ProductRepository repository;
    private final ModelGateway modelGateway;
    private final ImageResizer imageResizer;

    public ProductService(
            final ProductRepository repository,
            final ModelGateway modelGateway,
            final ImageResizer imageResizer) {
        this.repository = repository;
        this.modelGateway = modelGateway;
        this.imageResizer = imageResizer;
    }

    public DiyTaskInference inferTasks(final byte[] image, final MediaType mediaType) throws IOException {
        final byte[] resizedImage = imageResizer.resizeToLongestSide(image, MAXIMUM_IMAGE_DIMENSION);
        return modelGateway.inferTasks(resizedImage, mediaType);
    }

    public List<Product> recommendProducts(final String query) {
        LOGGER.debug("Recommend products query: {}", query);
        final RecommendationBrief recommended = modelGateway.recommend(query);

        if (!recommended.supported()) {
            LOGGER.warn("Recommend product not supported!");
            return Collections.emptyList();
        }

        final List<String> categories = recommended.productCategories();
        LOGGER.debug("Model recommended: {} categories", categories.size());

        final Map<Long, Product> productsById = new LinkedHashMap<>();
        for (final String category : categories) {
            final List<Product> products = searchProducts(category);
            LOGGER.debug(" - Category: '{}' matched {} products", category, products.size());
            products.forEach(product -> productsById.putIfAbsent(product.id(), product));
        }

        return List.copyOf(productsById.values());
    }

    public List<Product> searchProducts(final String query) {
        return repository.search(query);
    }
}
