package demo.external.catalogue;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"spring.cloud.circuitbreaker.resilience4j.disable-time-limiter=true",
                "spring.cloud.circuitbreaker.bulkhead.resilience4j.enabled=false"})
class CatalogueCircuitBreakerTest {

    @Autowired
    private CircuitBreakerFactory<?, ?> factory;

    @Autowired
    private CircuitBreakerRegistry registry;

    @Test
    void opensDuringRetriesAndAllowsOneRecoveryAttempt() {
        var restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity(anyString(), eq(CatalogueItemTo.class)))
                .thenThrow(new ResourceAccessException("Timed out", new SocketTimeoutException()));
        var gateway = new CatalogueItemGateway(restTemplate, factory);
        // The first operation fails twice; its third attempt is rejected by the open circuit.
        assertThatThrownBy(() -> gateway.findById(1)).isInstanceOf(CallNotPermittedException.class);
        verify(restTemplate, times(2)).getForEntity(anyString(), eq(CatalogueItemTo.class));
        assertThatThrownBy(() -> gateway.findById(1)).isInstanceOf(CallNotPermittedException.class);
        verify(restTemplate, times(2)).getForEntity(anyString(), eq(CatalogueItemTo.class));

        var breaker = registry.circuitBreaker("catalogue");
        breaker.transitionToHalfOpenState();
        // A failed half-open probe reopens the circuit before another HTTP attempt.
        assertThatThrownBy(() -> gateway.findById(1)).isInstanceOf(CallNotPermittedException.class);
        verify(restTemplate, times(3)).getForEntity(anyString(), eq(CatalogueItemTo.class));

        breaker.transitionToHalfOpenState();
        doReturn(org.springframework.http.ResponseEntity.ok().build())
                .when(restTemplate).getForEntity(anyString(), eq(CatalogueItemTo.class));
        gateway.findById(1);
        org.assertj.core.api.Assertions.assertThat(breaker.getState())
                .isEqualTo(io.github.resilience4j.circuitbreaker.CircuitBreaker.State.CLOSED);
        verify(restTemplate, times(4)).getForEntity(anyString(), eq(CatalogueItemTo.class));
    }
}
