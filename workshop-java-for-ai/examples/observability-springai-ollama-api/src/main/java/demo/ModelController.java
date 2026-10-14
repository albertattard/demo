package demo;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ModelController {

    private final ModelGateway gateway;

    ModelController(final ModelGateway gateway) {
        this.gateway = gateway;
    }

    @PostMapping("/prompt")
    public Assistant prompt(@RequestBody final Prompt prompt) {
        return gateway.prompt(prompt);
    }
}
