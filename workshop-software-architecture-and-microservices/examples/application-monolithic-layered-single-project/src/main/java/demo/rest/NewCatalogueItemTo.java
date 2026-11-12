package demo.rest;

import demo.dao.CatalogueItemEntity;

public record NewCatalogueItemTo(String caption, String description) {
    public CatalogueItemEntity toEntity() {
        return new CatalogueItemEntity(caption, description);
    }
}
