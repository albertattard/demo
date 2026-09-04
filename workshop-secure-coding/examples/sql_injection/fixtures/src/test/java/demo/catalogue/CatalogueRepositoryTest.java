package demo.catalogue;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CatalogueRepositoryTest {

    @Autowired
    private CatalogueRepository repository;

    @Test
    void returnTheItemMatchingTheGivenSearchTerm() {
        final var results = repository.search("chair");

        assertThat(results)
                .extracting(CatalogueItem::guid)
                .contains(UUID.fromString("3787efde-9365-4b4b-bd67-ce567393afb1"));
    }

    @Test
    void returnAnEmptyListAsNothingMatchesTheInjection() {
        final var results = repository.search("' union all select 1, id, name, pass from users where name like '");

        assertThat(results).isEmpty();
    }
}
