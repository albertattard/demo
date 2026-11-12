package demo.infrastructure;

import demo.external.catalogue.CatalogueItemTo;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Configuration
public class AmqpConfiguration {

    @Bean
    public MessageConverter jsonMessageConverter(final ClassMapper classMapper) {
        requireNonNull(classMapper, "Class mapper cannot be nullt");

        final Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public DefaultClassMapper classMapper() {
        final Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("demo.catalogue.CatalogueItemTo", CatalogueItemTo.class);

        final DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setIdClassMapping(idClassMapping);
        return classMapper;
    }
}
