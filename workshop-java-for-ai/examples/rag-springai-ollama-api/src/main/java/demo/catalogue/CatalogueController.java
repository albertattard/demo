package demo.catalogue;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Controller
public class CatalogueController {

    private final OfferService service;

    public CatalogueController(final OfferService service) {this.service = service;}

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q,
                         @RequestParam(required = false) BigDecimal minPrice,
                         @RequestParam(required = false) BigDecimal maxPrice,
                         final Model model) {
        model.addAttribute("offers", service.search(q, minPrice, maxPrice));
        return "search";
    }

    @GetMapping("/offer/{slug}")
    public String offer(final @PathVariable String slug, final Model model) {
        final OfferEntity offer = service.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
        model.addAttribute("offer", offer);
        return "offer";
    }
}
