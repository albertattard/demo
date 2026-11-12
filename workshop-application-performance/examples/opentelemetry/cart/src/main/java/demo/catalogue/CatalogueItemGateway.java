package demo.catalogue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
public class CatalogueItemGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogueItemGateway.class);

    private final RestTemplate restTemplate;

    public CatalogueItemGateway(final RestTemplate restTemplate) {
        this.restTemplate = requireNonNull(restTemplate, "Rest template cannot be null");
    }

    public Optional<CatalogueItemEntity> findById(final long id) {
        try {
            final ResponseEntity<CatalogueItemTo> entity = restTemplate.getForEntity("/catalogue/item/%d".formatted(id), CatalogueItemTo.class);
            if (HttpStatus.OK.equals(entity.getStatusCode())) {
                return Optional.ofNullable(entity.getBody())
                        .map(CatalogueItemTo::toEntity);
            }
        } catch (final HttpClientErrorException.NotFound e) {
            LOGGER.error("Catalogue Item with id {} not found", id, e);
        }
        return Optional.empty();
    }
}
