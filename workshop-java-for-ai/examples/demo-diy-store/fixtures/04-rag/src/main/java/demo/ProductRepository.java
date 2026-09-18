package demo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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

    public List<Product> findAll() {
        return jdbcTemplate.query("""
                        SELECT id, name, keywords, description
                        FROM product
                        ORDER BY id""",
                mapProduct());
    }

    public List<Product> findByIds(final List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        final String placeholders = ids.stream().map(_ -> "?").collect(Collectors.joining(", "));
        return jdbcTemplate.query("""
                        SELECT id, name, keywords, description
                        FROM product
                        WHERE id IN (""" + placeholders + ')',
                mapProduct(), ids.toArray());
    }

    private static RowMapper<Product> mapProduct() {
        return (resultSet, _) -> new Product(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("keywords"),
                resultSet.getString("description"));
    }
}
