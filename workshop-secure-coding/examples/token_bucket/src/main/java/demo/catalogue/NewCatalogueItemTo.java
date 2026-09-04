package demo.catalogue;

public record NewCatalogueItemTo(String caption, String description) {
    public CatalogueItemEntity toEntity() {
        return new CatalogueItemEntity(caption, description);
    }
}
