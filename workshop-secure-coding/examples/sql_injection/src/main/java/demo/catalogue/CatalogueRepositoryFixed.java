package demo.catalogue;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Profile("fixed")
public class CatalogueRepositoryFixed implements CatalogueRepository {

    private final DataSource dataSource;

    public CatalogueRepositoryFixed(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<CatalogueItem> search(String searchTerm) {
        final String query = """
                select ID, GUID, CAPTION, DESCRIPTION
                from catalogue_item
                where DESCRIPTION like ?""";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = statement.executeQuery()) {
                final List<CatalogueItem> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(new CatalogueItem(
                            rs.getLong(1),
                            UUID.fromString(rs.getString(2)),
                            rs.getString(3),
                            rs.getString(4)));
                }
                return result;
            }
        } catch (final SQLException e) {
            throw new RuntimeException("Failed to execute query", e);
        }
    }
}
