package demo.external.catalogue;

import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

@Configuration
public class CatalogueConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogueConfiguration.class);

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> catalogueCircuitBreaker() {
        return factory -> {
            factory.configure(builder -> builder.circuitBreakerConfig(CircuitBreakerConfig.custom()
                    .slidingWindowSize(2)
                    .minimumNumberOfCalls(2)
                    .failureRateThreshold(50)
                    .waitDurationInOpenState(Duration.ofSeconds(30))
                    .permittedNumberOfCallsInHalfOpenState(1)
                    .ignoreException(failure -> {
                        while ((failure instanceof ExecutionException
                                || failure instanceof CompletionException)
                                && failure.getCause() != null) failure = failure.getCause();
                        return failure instanceof HttpClientErrorException.NotFound;
                    })
                    .build()), "catalogue");
            factory.addCircuitBreakerCustomizer(breaker -> breaker.getEventPublisher()
                    .onStateTransition(event -> LOGGER.info("Catalogue circuit breaker {}", event.getStateTransition()))
                    .onCallNotPermitted(event -> LOGGER.info("Catalogue circuit breaker rejected a call")), "catalogue");
        };
    }

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
