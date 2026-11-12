package demo.catalogue;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/// Makes overlapping catalogue requests visible during the cascading-failure
/// exercise. When the cart times out and retries, earlier requests may still be
/// running; the logged active count shows concurrent requests passing through
/// this filter, not the number of database connections in use.
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private final AtomicInteger active = new AtomicInteger();

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws ServletException, IOException {
        int count = active.incrementAndGet();
        LOG.info("Catalogue started {} active={}", request.getRequestURI(), count);
        try {
            chain.doFilter(request, response);
        } finally {
            LOG.info("Catalogue finished {} active={}", request.getRequestURI(), active.decrementAndGet());
        }
    }
}
