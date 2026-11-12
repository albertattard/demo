package demo.external.catalogue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.retry.*;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;

@Service
public class CatalogueItemGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogueItemGateway.class);

    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;
    private final RetryTemplate retryTemplate;

    public CatalogueItemGateway(final RestTemplate restTemplate, final CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.restTemplate = requireNonNull(restTemplate, "Rest template cannot be null");
        this.circuitBreaker = circuitBreakerFactory.create("catalogue");
        final RetryPolicy policy = RetryPolicy.builder()
                .maxRetries(2)
                .delay(Duration.ZERO)
                .predicate(CatalogueItemGateway::isTimeout)
                .build();
        this.retryTemplate = new RetryTemplate(policy);
        this.retryTemplate.setRetryListener(new RetryListener() {
            @Override
            public void beforeRetry(final RetryPolicy retryPolicy, final Retryable<?> retryable, final RetryState state) {
                LOGGER.info("Catalogue attempt {} timed out; retrying", state.getRetryCount());
            }
        });
    }

    public Optional<CatalogueItemEntity> findById(final long id) {
        try {
            final ResponseEntity<CatalogueItemTo> entity = withCircuitBreaker(() -> restTemplate.getForEntity("/catalogue/item/" + id, CatalogueItemTo.class));
            if (HttpStatus.OK.equals(entity.getStatusCode())) {
                return Optional.ofNullable(entity.getBody())
                        .map(CatalogueItemTo::toEntity);
            }
        } catch (final HttpClientErrorException.NotFound e) {
            LOGGER.error("Catalogue Item with id {} not found", id, e);
        }
        return Optional.empty();
    }

    public List<CatalogueItemEntity> findAllById(final Set<Long> ids) {
        final String stringIds = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        final ParameterizedTypeReference<List<CatalogueItemTo>> type = new ParameterizedTypeReference<>() {};
        return withCircuitBreaker(() -> restTemplate.exchange("/catalogue/item/all/" + stringIds, HttpMethod.GET, null, type))
                .getBody() /* TODO: need to check for nulls */
                .stream().map(CatalogueItemTo::toEntity)
                .toList();
    }

    private <T> T withCircuitBreaker(final Supplier<T> request) {
        final AtomicInteger attempts = new AtomicInteger();
        return retryTemplate.invoke(() -> circuitBreaker.run(() -> {
            LOGGER.info("Catalogue attempt {}", attempts.incrementAndGet());
            return request.get();
        }, CatalogueItemGateway::propagateUnwrappedFailure));
    }

    private static boolean isTimeout(final Throwable failure) {
        if (!(failure instanceof ResourceAccessException)) return false;
        Throwable cause = failure;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause instanceof SocketTimeoutException || cause instanceof HttpTimeoutException;
    }

    private static <T> T propagateUnwrappedFailure(Throwable failure) {
        while ((failure instanceof ExecutionException || failure instanceof CompletionException)
                && failure.getCause() != null) {
            failure = failure.getCause();
        }

        if (failure instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new IllegalStateException("Catalogue call failed", failure);
    }
}
