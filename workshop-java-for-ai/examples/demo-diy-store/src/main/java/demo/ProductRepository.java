package demo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Product> search(final String query) {
        final String pattern = "%" + query.strip().toLowerCase(Locale.ROOT) + "%";
        return jdbcTemplate.query("""
                        SELECT id, name, keywords, description
                        FROM product
                        WHERE LOWER(name) LIKE ?
                           OR LOWER(keywords) LIKE ?
                        ORDER BY name""",
                mapProduct(), pattern, pattern);
    }

    private static RowMapper<Product> mapProduct() {
        return (resultSet, _) -> new Product(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("keywords"),
                resultSet.getString("description"));
    }
}
