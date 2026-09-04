package demo.catalogue;

import java.util.List;

public interface CatalogueRepository {

    List<CatalogueItem> search(String searchTerm);
}
