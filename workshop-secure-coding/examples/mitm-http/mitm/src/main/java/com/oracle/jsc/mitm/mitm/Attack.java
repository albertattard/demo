package com.oracle.jsc.mitm.mitm;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages = "com.oracle.jsc.mitm.mitm")
@RestController
public class Attack 
{
    private final AppService appService;
    
    public Attack(AppService appService) {
        this.appService = appService;
    }

    @GetMapping("/")
    public String home() {
      return appService.message();
    }

    public static void main(String[] args) {
      SpringApplication.run(Attack.class, args);
    }
}
