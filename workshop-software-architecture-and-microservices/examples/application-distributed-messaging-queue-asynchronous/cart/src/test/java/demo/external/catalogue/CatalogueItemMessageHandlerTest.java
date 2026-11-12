package demo.external.catalogue;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CatalogueItemMessageHandlerTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CatalogueRepository repository;

    @Test
    void saveTheNewCatalogueItemToDatabase() {
        /* Given */
        final long itemId = 6L;
        final String itemCaption = "Green Plant";
        assertThat(repository.findById(itemId))
                .describedAs("Catalogue item %s with id %d should not exist before the message is sent", itemCaption, itemId)
                .isEmpty();

        /* When */
        rabbitTemplate.convertAndSend("demo-exchange", "demo.catalogue.new", newCatalogueItemMessage(itemId, itemCaption, "The description is dropped"));
        allowForTheMessageToBeDelivered();

        /* Then */
        final Optional<CatalogueItemEntity> item = repository.findById(itemId);
        assertThat(item)
                .describedAs("The item should be saved in the table")
                .isPresent();
        assertThat(item.get())
                .describedAs("The item should match the message data")
                .isEqualTo(new CatalogueItemEntity(itemId, itemCaption));
    }

    private static Message newCatalogueItemMessage(final long id, final String caption, final String description) {
        /* TODO: Escape the text */
        final byte[] body = """
                {"id":%d,"caption":"%s","description":"%s"}"""
                .formatted(id, caption, description)
                .getBytes(StandardCharsets.UTF_8);

        final MessageProperties properties = new MessageProperties();
        properties.setHeader("__TypeId__", "demo.catalogue.CatalogueItemTo");
        properties.setContentType("application/json");
        properties.setContentEncoding("UTF-8");
        properties.setContentLength(body.length);
        properties.setReceivedDeliveryMode(MessageDeliveryMode.PERSISTENT);
        properties.setPriority(0);
        properties.setRedelivered(false);
        properties.setReceivedExchange("demo-exchange");
        properties.setReceivedRoutingKey("demo.catalogue.new");
        properties.setDeliveryTag(1);

        return new Message(body, properties);
    }

    private static void allowForTheMessageToBeDelivered() {
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for the message to go through", e);
        }
    }
}
