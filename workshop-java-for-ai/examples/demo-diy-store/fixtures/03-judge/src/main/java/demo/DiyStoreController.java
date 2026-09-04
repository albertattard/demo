package demo;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class DiyStoreController {

    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;

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

    @PostMapping("/recommend")
    public String recommend(
            @Valid @ModelAttribute final ProductSearch productSearch,
            final BindingResult bindingResult,
            final Model model) {
        addSearchForm(model);
        if (bindingResult.hasErrors()) {
            return "index";
        }
        model.addAttribute("products", service.recommendProducts(productSearch.getQuery()));
        return "index";
    }

    @PostMapping("/infer-tasks")
    public String inferTasks(
            @RequestParam("photo") final MultipartFile photo,
            final Model model) throws IOException {
        addSearchForm(model);
        if (photo.isEmpty()) {
            model.addAttribute("photoError", "Choose a JPEG image to analyse.");
            return "index";
        }
        if (!MediaType.IMAGE_JPEG_VALUE.equals(photo.getContentType())) {
            model.addAttribute("photoError", "Upload a JPEG image.");
            return "index";
        }
        if (photo.getSize() > MAX_IMAGE_SIZE_BYTES) {
            model.addAttribute("photoError", "Upload a JPEG image smaller than 5 MB.");
            return "index";
        }

        model.addAttribute("taskInference", service.inferTasks(photo.getBytes(), MediaType.parseMediaType(photo.getContentType())));
        return "index";
    }

    private void addSearchForm(final Model model) {
        if (!model.containsAttribute("productSearch")) {
            model.addAttribute("productSearch", new ProductSearch());
        }
    }
}
