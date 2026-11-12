package demo.quote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class QuoteRepositoryTest {

    @Autowired
    private QuoteRepository repository;

    @Test
    public void listAll() {
        assertThat(repository.count())
                .describedAs("Flyway loads the 200 quotes")
                .isEqualTo(200);

        assertThat(repository.findAll())
                .describedAs("The random-quote endpoint can materialise the quotes")
                .hasSize(200);
    }
}
