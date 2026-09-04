package demo;

import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTracing(export = false)
public @interface SpringBootTestWithTestProfile {

    @AliasFor(annotation = SpringBootTest.class, attribute = "properties")
    String[] properties() default {
            "management.tracing.export.otlp.enabled=false",
            "management.otlp.metrics.export.enabled=false"
    };

    @AliasFor(annotation = SpringBootTest.class, attribute = "webEnvironment")
    SpringBootTest.WebEnvironment webEnvironment() default SpringBootTest.WebEnvironment.MOCK;
}
