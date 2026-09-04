package demo.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public final class UiController {
    @GetMapping("/")
    public String viewHomePage(Model model) {
        model.addAttribute("DemoString", new DemoString());
        return "index";
    }
    
    @PostMapping("/")
    public String viewSearchResult(@ModelAttribute DemoString demoString, Model model) {
        model.addAttribute("DemoString", demoString);
        return "index";
    }
    
}
