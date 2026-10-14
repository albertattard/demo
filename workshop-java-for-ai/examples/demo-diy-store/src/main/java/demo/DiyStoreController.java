package demo;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class DiyStoreController {

    private final ProductService service;

    public DiyStoreController(final ProductService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String showSearchForm(final Model model) {
        addSearchForm(model);
        return "index";
    }

    @GetMapping("/search")
    public String search(
            @Valid @ModelAttribute final ProductSearch productSearch,
            final BindingResult bindingResult,
            final Model model) {
        addSearchForm(model);
        if (bindingResult.hasErrors()) {
            return "index";
        }
        model.addAttribute("products", service.searchProducts(productSearch.getQuery()));
        return "index";
    }

    private void addSearchForm(final Model model) {
        if (!model.containsAttribute("productSearch")) {
            model.addAttribute("productSearch", new ProductSearch());
        }
    }
}
