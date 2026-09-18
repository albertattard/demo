package demo.chat.spring;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Configuration
class RagConfiguration {

    private final ResourceLoader resourceLoader;

    RagConfiguration(final ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    RestClientCustomizer logbookCustomizer(final LogbookClientHttpRequestInterceptor interceptor) {
        return restClient -> restClient.requestInterceptor(interceptor);
    }

    /// The `QuestionAnswerAdvisor` is a basic Retrieval-Augmented Generation (RAG) system that forwards the user’s
    /// query directly to the vector store without modification. In some cases, the query may be a continuation of the
    /// ongoing conversation—for example, _“how much does that cost?”_. Relying solely on this fragment of text makes it
    /// difficult to obtain reliable results, and the documents retrieved may therefore be irrelevant.
    @Bean
    @Profile("BASIC")
    QuestionAnswerAdvisor questionAnswerAdvisor(final VectorStore store) {
        /* TODO: Read these values from the properties */
        return QuestionAnswerAdvisor.builder(store)
                .searchRequest(SearchRequest.builder()
                        .topK(8)
                        .similarityThreshold(0.50)
                        .build())
                .build();
    }

    /// The `RetrievalAugmentationAdvisor` is an advanced Retrieval-Augmented Generation (RAG) component that considers
    /// both the user’s query and the preceding conversation history. By doing so, it maintains continuity and is better
    /// suited to handling follow-up questions such as “how much would that cost?”. This makes it the preferred RAG
    /// advisor for conversational use cases.
    @Bean
    @Profile("!BASIC")
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(final ChatClient.Builder builder, final DocumentRetriever retriever) {
        // 1) Turn follow-ups into standalone, search-optimized queries.
        final RewriteQueryTransformer rewrite = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();

        // 2) Folds history in
        final CompressionQueryTransformer compress = CompressionQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();

        // 3) Templates for the augmenter.
        // Required placeholders for ContextualQueryAugmenter are {query} and {context}.
        final PromptTemplate contextTemplate = PromptTemplate.builder()
                .template(prompt("context"))
                .build();

        final PromptTemplate emptyTemplate = PromptTemplate.builder()
                .template(prompt("empty-context"))
                .build();

        // 4) Build a custom ContextualQueryAugmenter.
        final ContextualQueryAugmenter augmenter = ContextualQueryAugmenter.builder()
                .promptTemplate(contextTemplate)
                .emptyContextPromptTemplate(emptyTemplate)
                .documentFormatter(DocumentFormatter.instance())
                .allowEmptyContext(true)
                .build();

        // 5) Put it together.
        return RetrievalAugmentationAdvisor.builder()
                .queryTransformers(rewrite, compress) // order matters: rewrite, then compress
                .queryAugmenter(augmenter)
                .documentRetriever(retriever)
                .build();
    }

    @Bean
    DocumentRetriever documentRetriever(final VectorStore vectorStore) {
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(8)
                .similarityThreshold(0.50)
                .build();
    }

    @Bean
    VectorStore vectorStore(final EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel)
                .build();
    }

    @Bean
    public String systemPrompt() {
        return prompt("system");
    }

    private String prompt(final String name) {
        try {
            return resourceLoader.getResource("classpath:/prompts/" + name + ".prompt")
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to load prompt " + name, e);
        }
    }
}
