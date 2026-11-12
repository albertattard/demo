package demo.external.catalogue;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

@Service
public class CatalogueItemMessageHandler {

    private final CatalogueRepository repository;

    public CatalogueItemMessageHandler(final CatalogueRepository repository) {
        this.repository = requireNonNull(repository, "Catalogue repository cannot be null");
    }

    @RabbitListener(queues = "demo-queue")
    public void newCatalogueItem(final CatalogueItemTo item) {
        requireNonNull(item, "Catalogue item cannot be null");
        repository.save(item.toEntity());
    }
}
