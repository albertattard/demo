package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LocalStoreTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalStoreTools.class);
    private static final int RESULT_LIMIT = 10;

    private final CatalogueRetriever catalogueRetriever;
    private final ProductRepository repository;

    public LocalStoreTools(
            final CatalogueRetriever catalogueRetriever,
            final ProductRepository repository) {
        this.catalogueRetriever = catalogueRetriever;
        this.repository = repository;
    }

    @Tool(name = "search_catalogue",
            description = "Search the DIY catalogue for products relevant to a customer request. Returns catalogue products with IDs, names and descriptions.")
    public List<Product> searchCatalogue(final String query) {
        final List<Long> ids = catalogueRetriever.retrieve(query).stream()
                .map(RetrievedProduct::productId)
                .distinct()
                .limit(RESULT_LIMIT)
                .toList();
        final Map<Long, Product> products = repository.findByIds(ids).stream()
                .collect(Collectors.toMap(Product::id, Function.identity()));
        final List<Product> result = ids.stream()
                .map(products::get)
                .filter(Objects::nonNull)
                .toList();
        LOGGER.info("Local tool search_catalogue returned {}", result);
        return result;
    }
}
