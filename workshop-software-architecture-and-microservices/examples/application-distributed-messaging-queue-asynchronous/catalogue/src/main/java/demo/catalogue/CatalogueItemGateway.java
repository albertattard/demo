package demo.catalogue;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

@Service
public class CatalogueItemGateway {

    private final RabbitTemplate rabbitTemplate;

    public CatalogueItemGateway(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = requireNonNull(rabbitTemplate, "The rabbit template cannot be null");
    }

    public void newCatalogueItem(final CatalogueItemEntity item) {
        requireNonNull(item, "The catalogue item entry cannot be null");

        rabbitTemplate.convertAndSend("demo-exchange", "demo.catalogue.new", CatalogueItemTo.of(item));
    }
}
