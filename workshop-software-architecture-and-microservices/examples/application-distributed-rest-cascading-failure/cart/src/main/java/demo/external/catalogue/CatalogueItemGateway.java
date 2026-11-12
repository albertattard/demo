package demo.external.catalogue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.retry.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
public class CatalogueItemGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogueItemGateway.class);

    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;

    public CatalogueItemGateway(final RestTemplate restTemplate) {
        this.restTemplate = requireNonNull(restTemplate, "Rest template cannot be null");
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
                LOGGER.info("Catalogue attempt {}", state.getRetryCount() + 1);
            }
        });
    }

    public Optional<CatalogueItemEntity> findById(final long id) {
        try {
            final ResponseEntity<CatalogueItemTo> entity = attempt(() -> restTemplate.getForEntity("/catalogue/item/" + id, CatalogueItemTo.class));
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
        return attempt(() -> restTemplate.exchange("/catalogue/item/all/" + stringIds, HttpMethod.GET, null, type))
                .getBody() /* TODO: need to check for nulls */
                .stream().map(CatalogueItemTo::toEntity)
                .toList();
    }

    private <T> T attempt(final Supplier<T> request) {
        LOGGER.info("Catalogue attempt 1");
        return retryTemplate.invoke(request);
    }

    private static boolean isTimeout(final Throwable failure) {
        if (!(failure instanceof ResourceAccessException)) return false;
        Throwable cause = failure;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause instanceof SocketTimeoutException || cause instanceof HttpTimeoutException;
    }
}
