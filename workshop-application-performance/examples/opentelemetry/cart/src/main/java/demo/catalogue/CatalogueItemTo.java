package demo.catalogue;

public record CatalogueItemTo(long id, String caption) {

    public CatalogueItemEntity toEntity() {
        return new CatalogueItemEntity(id, caption);
    }
}
