package demo.chat.spring;

import demo.catalogue.OfferEntity;
import demo.catalogue.OfferService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * TODO: Move to a persistent store
 *
 * Takes all the brochures and added them to the in-memory vector store on startup
 */
@Component
class VectorBootstrap implements ApplicationRunner {

    private final OfferService offerService;
    private final VectorStore store;
    private final DocumentConvertor<OfferEntity> converter;

    VectorBootstrap(final OfferService offerService, final VectorStore store, final DocumentConvertor<OfferEntity> converter) {
        this.offerService = offerService;
        this.store = store;
        this.converter = converter;
    }

    @Override
    public void run(final ApplicationArguments args) {
        final List<OfferEntity> entities = offerService.findAll();
        if (entities.isEmpty()) {
            return;
        }

        final List<Document> documents = entities.stream()
                .flatMap(converter)
                .toList();
        store.add(documents);
    }
}
