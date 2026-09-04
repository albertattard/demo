package demo;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagConfiguration {

    @Bean
    VectorStore vectorStore(final EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    @ConditionalOnProperty(name = "demo.rag.load-catalogue", havingValue = "true", matchIfMissing = true)
    ApplicationRunner loadCatalogue(final VectorStore vectorStore,
                                    final ProductRepository repository,
                                    final CatalogueDocuments catalogueDocuments) {
        return (ApplicationArguments _) ->
                vectorStore.add(catalogueDocuments.mapDocuments(repository.findAll()));
    }
}
