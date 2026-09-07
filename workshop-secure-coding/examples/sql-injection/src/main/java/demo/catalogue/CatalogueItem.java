package demo.catalogue;

import java.util.UUID;

public record CatalogueItem(
        Long id,
        UUID guid,
        String caption,
        String description) {}
