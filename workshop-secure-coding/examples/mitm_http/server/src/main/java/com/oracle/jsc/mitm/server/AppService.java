package com.oracle.jsc.mitm.server;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(ServiceProperties.class)
public class AppService {

    private final ServiceProperties serviceProperties;

    public AppService(ServiceProperties serviceProperties) {
      this.serviceProperties = serviceProperties;
    }

    public String message() {
      return this.serviceProperties.getMessage();
    }
}
