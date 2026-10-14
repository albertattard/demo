package demo;

public record RetrievedProduct(
        long productId,
        String content,
        double score) implements Comparable<RetrievedProduct> {

    public RetrievedProduct {
        if (!Double.isFinite(score)) {
            throw new IllegalArgumentException("Score must be finite");
        }
    }

    @Override
    public int compareTo(final RetrievedProduct other) {
        return Double.compare(other.score, score);
    }

    public RetrievedProduct betterOf(final RetrievedProduct other) {
        if (productId != other.productId) {
            throw new IllegalArgumentException("Products must have the same product id");
        }

        return score >= other.score ? this : other;
    }
}
