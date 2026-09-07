package com.oracle.jsc.svc_a;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.oracle.jsc.svc_a")
public class ServiceA 
{
    public static void main(String[] args) {
      SpringApplication.run(ServiceA.class, args);
    }
}
