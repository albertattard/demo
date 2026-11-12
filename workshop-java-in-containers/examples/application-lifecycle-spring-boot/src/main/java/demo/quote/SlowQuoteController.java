package demo.quote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/quote")
public class SlowQuoteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlowQuoteController.class);

    private final QuoteService service;

    public SlowQuoteController(final QuoteService service) {
        this.service = requireNonNull(service, "The quote service cannot be null");
    }

    @GetMapping("/slow")
    public ResponseEntity<Quote> slow(@RequestParam(defaultValue = "8") final int seconds) {
        if (seconds < 0 || seconds > 30) {
            return ResponseEntity.badRequest().build();
        }

        LOGGER.info("Slow quote request started; waiting {} seconds", seconds);
        try {
            Thread.sleep(seconds * 1_000L);
        } catch (final InterruptedException _) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        LOGGER.info("Slow quote request completed");
        return service.random()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
