package com.oracle.jsc.mitm.mitm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import com.oracle.jsc.mitm.mitm.AppService;

@SpringBootTest("service.message=Hello")
public class AppServiceTest {
    @Autowired
    private AppService appService;

    @Test
    public void contextLoads() {
      assertThat(appService.message()).isNotNull();
    }

    @SpringBootApplication
    static class TestConfiguration {
    }
}
