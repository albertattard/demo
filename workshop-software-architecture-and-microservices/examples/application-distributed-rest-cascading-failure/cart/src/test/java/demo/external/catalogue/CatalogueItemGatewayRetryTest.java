package demo.external.catalogue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatalogueItemGatewayRetryTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final CatalogueItemGateway gateway = new CatalogueItemGateway(restTemplate);

    @Test
    void retriesSocketTimeoutTwiceAndPropagatesTheLastException() {
        var failure = new ResourceAccessException("Timed out", new SocketTimeoutException());
        when(restTemplate.getForEntity(anyString(), eq(CatalogueItemTo.class))).thenThrow(failure);

        assertThatThrownBy(() -> gateway.findById(1)).isSameAs(failure);
        verify(restTemplate, times(3)).getForEntity(anyString(), eq(CatalogueItemTo.class));
    }

    @Test
    void retriesHttpTimeoutAndReturnsOnSuccess() {
        var failure = new ResourceAccessException("Timed out", new HttpTimeoutException("Timed out"));
        when(restTemplate.getForEntity(anyString(), eq(CatalogueItemTo.class)))
                .thenThrow(failure)
                .thenReturn(org.springframework.http.ResponseEntity.ok().build());

        assertThat(gateway.findById(1)).isEmpty();
        verify(restTemplate, times(2)).getForEntity(anyString(), eq(CatalogueItemTo.class));
    }

    @Test
    void doesNotRetryOtherResourceAccessErrors() {
        var failure = new ResourceAccessException("Connection failed", new IOException());
        when(restTemplate.getForEntity(anyString(), eq(CatalogueItemTo.class))).thenThrow(failure);

        assertThatThrownBy(() -> gateway.findById(1)).isSameAs(failure);
        verify(restTemplate).getForEntity(anyString(), eq(CatalogueItemTo.class));
    }

    @Test
    void doesNotRetryNotFound() {
        when(restTemplate.getForEntity(anyString(), eq(CatalogueItemTo.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not found", null, null, null));

        assertThat(gateway.findById(1)).isEmpty();
        verify(restTemplate).getForEntity(anyString(), eq(CatalogueItemTo.class));
    }

    @Test
    void doesNotRetryWhenTimeoutIsNotTheDeepestCause() {
        var timeout = new SocketTimeoutException();
        timeout.initCause(new IOException("Underlying error"));
        var failure = new ResourceAccessException("Failed", timeout);
        when(restTemplate.getForEntity(anyString(), eq(CatalogueItemTo.class))).thenThrow(failure);

        assertThatThrownBy(() -> gateway.findById(1)).isSameAs(failure);
        verify(restTemplate).getForEntity(anyString(), eq(CatalogueItemTo.class));
    }
}
