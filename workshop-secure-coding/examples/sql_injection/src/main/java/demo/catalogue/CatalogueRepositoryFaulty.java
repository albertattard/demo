package demo.catalogue;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Profile("!fixed")
public class CatalogueRepositoryFaulty implements CatalogueRepository {

    private final DataSource dataSource;

    public CatalogueRepositoryFaulty(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<CatalogueItem> search(final String searchTerm) {
        final String query = """
                select ID, GUID, CAPTION, DESCRIPTION
                from catalogue_item
                where DESCRIPTION like '%""" + searchTerm + "%'";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            final List<CatalogueItem> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new CatalogueItem(
                        rs.getLong(1),
                        UUID.fromString(rs.getString(2)),
                        rs.getString(3),
                        rs.getString(4)));
            }
            return result;
        } catch (final SQLException e) {
            throw new RuntimeException("Failed to execute query", e);
        }
    }
}
