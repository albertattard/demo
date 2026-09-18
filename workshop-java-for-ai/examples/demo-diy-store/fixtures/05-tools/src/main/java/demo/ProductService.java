package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    private static final int MAXIMUM_IMAGE_DIMENSION = 1024;

    private final ProductRepository repository;
    private final ModelGateway modelGateway;
    private final ImageResizer imageResizer;
    private final CatalogueRetriever catalogueRetriever;
    private final boolean toolsEnabled;

    public ProductService(
            final ProductRepository repository,
            final ModelGateway modelGateway,
            final ImageResizer imageResizer,
            final CatalogueRetriever catalogueRetriever,
            @Value("${demo.tools.enabled:false}") final boolean toolsEnabled) {
        this.repository = repository;
        this.modelGateway = modelGateway;
        this.imageResizer = imageResizer;
        this.catalogueRetriever = catalogueRetriever;
        this.toolsEnabled = toolsEnabled;
    }

    public DiyTaskInference inferTasks(final byte[] image, final MediaType mediaType) throws IOException {
        final byte[] resizedImage = imageResizer.resizeToLongestSide(image, MAXIMUM_IMAGE_DIMENSION);
        final DiyTaskInference inference = modelGateway.inferTasks(resizedImage, mediaType);
        if (inference == null) {
            LOGGER.warn("The task-inference model did not return a result.");
            return new DiyTaskInference(false, List.of());
        }

        LOGGER.debug("Model's inferred tasks: {}", inference);

        if (!inference.supported()) {
            return new DiyTaskInference(false, List.of());
        }

        try {
            final TaskInferenceSafetyReview review = modelGateway.judgeTaskChoices(inference);
            if (review.passed()) {
                LOGGER.debug("The task-choice judge accepted the model output: {}", review.reason());
                return inference;
            }

            LOGGER.warn("The task-choice judge rejected the model output: {}", review.reason());
        } catch (final RuntimeException e) {
            LOGGER.warn("The task-choice judge did not return a decision.", e);
        }

        return new DiyTaskInference(false, List.of());
    }

    public List<Product> recommendProducts(final String query) {
        return toolsEnabled
                ? recommendProductsWithTools(query)
                : recommendProductsWithRag(query);
    }

    private List<Product> recommendProductsWithRag(final String query) {
        LOGGER.debug("Recommend products query: {}", query);
        final RetrievalQueries retrievalQueries = modelGateway.createRetrievalQueries(query);

        if (!retrievalQueries.supported()) {
            LOGGER.warn("Recommend product not supported!");
            return List.of();
        }

        // For each product ID, retain the highest-scoring retrieval.
        final List<RetrievedProduct> retrievedProducts = retrievalQueries.queries().stream()
                .flatMap(q -> catalogueRetriever.retrieve(q).stream())
                .collect(Collectors.toMap(
                        RetrievedProduct::productId,
                        Function.identity(),
                        RetrievedProduct::betterOf))
                .values().stream()
                .sorted()
                .toList();
        final ProductRecommendation recommendation = modelGateway.selectRetrievedProducts(query, retrievedProducts);

        // Verify that the module only returned a subset of the provided product ids.
        if (!retrievedProducts.stream().map(RetrievedProduct::productId).collect(Collectors.toSet())
                .containsAll(recommendation.productIds())) {
            LOGGER.warn("The model named a product ID that was not retrieved. The recommendation was rejected.");
            return List.of();
        }

        // Deliberately ignore the model’s product-ID order: the prompt does not define it as a relevance ranking.
        return repository.findByIds(recommendation.productIds());
    }

    private List<Product> recommendProductsWithTools(final String customerRequest) {
        // The model receives only this request. Catalogue documents first enter its context through search_catalogue.
        final ProductRecommendation recommendation = modelGateway.recommendProductsWithTools(customerRequest);
        if (recommendation == null) {
            return List.of();
        }

        final List<Long> retrievedProductIds = catalogueRetriever.retrieve(customerRequest).stream()
                .map(RetrievedProduct::productId)
                .distinct()
                .toList();

        // Verify that the module only returned a subset of the available product ids.
        if (!retrievedProductIds.containsAll(recommendation.productIds())) {
            LOGGER.warn("The model selected a product ID that catalogue retrieval did not return. The answer was rejected.");
            return List.of();
        }

        return repository.findByIds(recommendation.productIds());
    }

    public List<Product> searchProducts(final String query) {
        return repository.search(query);
    }
}
