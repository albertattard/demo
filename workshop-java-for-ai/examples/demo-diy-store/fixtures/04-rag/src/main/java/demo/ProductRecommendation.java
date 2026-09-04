package demo;

import java.util.List;

public record ProductRecommendation(List<Long> productIds) {
    public ProductRecommendation {
        productIds = List.copyOf(productIds);
    }
}
