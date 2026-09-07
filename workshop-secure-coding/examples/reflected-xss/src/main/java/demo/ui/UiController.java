package demo.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public final class UiController {
    @GetMapping("/")
    public String viewHomePage(final Model model) {
        model.addAttribute("UserInput", new UserInput(""));
        return "index";
    }

    @PostMapping("/")
    public String viewSearchResult(@ModelAttribute final UserInput userInput, final Model model) {
        model.addAttribute("UserInput", userInput);
        return "index";
    }
}
