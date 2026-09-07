package demo.catalogue;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public final class CatalogueController {

    private final CatalogueRepository repository;

    public CatalogueController(final CatalogueRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public String viewHomePage(final Model model) {
        model.addAttribute("SearchTerm", new SearchTerm(""));
        model.addAttribute("catalogueItems", repository.search(""));
        return "index";
    }

    @PostMapping("/")
    public String viewSearchResult(final @ModelAttribute SearchTerm searchTerm, final Model model) {
        model.addAttribute("SearchTerm", searchTerm);
        model.addAttribute("catalogueItems", repository.search(searchTerm.term()));
        return "index";
    }
}
