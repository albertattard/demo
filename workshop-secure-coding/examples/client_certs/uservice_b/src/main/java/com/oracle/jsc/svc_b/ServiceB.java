package com.oracle.jsc.svc_b;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.oracle.jsc.svc_b")
public class ServiceB 
{
    public static void main(String[] args) {
      SpringApplication.run(ServiceB.class, args);
    }
}
