package demo.bola;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class OrderController {

    private final OrderService service;

    OrderController(final OrderService service) {
        this.service = service;
    }

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @GetMapping("/")
    String home(final Principal principal, final Model model) {
        model.addAttribute("username", principal.getName());
        model.addAttribute("orders", service.findOrdersFor(principal.getName()));
        return "index";
    }

    @GetMapping("/order/{id}")
    String getOrder(final @PathVariable Long id, final Principal principal, final Model model) {
        model.addAttribute("username", principal.getName());
        model.addAttribute("order", service.findOrder(id));
        model.addAttribute("mode", "vulnerable");
        return "order";
    }

    @PostMapping("/order/{id}")
    String updateOrder(final @PathVariable Long id, final @RequestParam String description) {
        service.updateDescription(id, description);
        return "redirect:/order/" + id;
    }
}
