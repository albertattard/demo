package com.oracle.jsc.svc_b;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableConfigurationProperties(ServiceProperties.class)
public class HelloController {
    private final ServiceProperties serviceProperties;

    public HelloController(ServiceProperties serviceProperties) {
        this.serviceProperties = serviceProperties;
    }

    @GetMapping(path="/hello")
    public Hello sayHello() {
        return new Hello(serviceProperties.getMessage());
    }

}
