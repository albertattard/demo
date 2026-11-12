package demo.external.catalogue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class CatalogueConfiguration {

    @Bean
    public RestTemplate catalogueRestTemplate(
            final RestTemplateBuilder builder,
            @Value("${catalogue.root-uri:http://localhost/}") final String catalogueRootUri) {
        return builder
                .connectTimeout(Duration.ofSeconds(1))
                .readTimeout(Duration.ofSeconds(1))
                .rootUri(catalogueRootUri)
                .build();
    }
}
