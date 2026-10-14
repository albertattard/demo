package demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findsProductsByKeywordFromTheDatabase() {
        assertThat(productRepository.search("living room"))
                .extracting(Product::name)
                .containsExactly("Interior matt paint");
    }
}
