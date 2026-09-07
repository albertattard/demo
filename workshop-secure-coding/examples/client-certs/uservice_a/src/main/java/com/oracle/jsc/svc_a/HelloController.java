package com.oracle.jsc.svc_a;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import reactor.core.publisher.Mono;

@RestController
@EnableConfigurationProperties(ServiceProperties.class)
public class HelloController {
    private final ServiceProperties serviceProperties;
    private final WebClient webClient;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public HelloController(ServiceProperties serviceProperties, WebClient webClient) {
        this.serviceProperties = serviceProperties;
        this.webClient = webClient;
    }

    @GetMapping(path="/hello")
    public Hello sayHello() {
        return new Hello(serviceProperties.getMessage());
    }

    @GetMapping(path="/hello_b")
    public Hello sayHelloToB() {
        Mono<String> fromServiceB = webClient.get()
                .uri("https://uhost-b:444/hello")
                .retrieve()
                .bodyToMono(String.class);

        try {
            String result = fromServiceB.block();
            Map<String, Object> mapFromB = new BasicJsonParser().parseMap(result);

            return new Hello(mapFromB.get("message") + " -- " + serviceProperties.getMessage());
        } catch (WebClientException e) {
            log.error(e.getMessage(), e);
            return new Hello("Web client got an exception: " + e.getMessage());
        }
    }

}
