package demo;

import java.util.List;

public record RecommendationBrief(
        boolean supported,
        List<String> productCategories) {}
