package demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetrievedProductTest {

    @Test
    void sortsByScoreWithTheHighestScoreFirst() {
        final RetrievedProduct lowest = new RetrievedProduct(1L, "Lowest score", 0.2D);
        final RetrievedProduct highest = new RetrievedProduct(2L, "Highest score", 0.9D);
        final RetrievedProduct middle = new RetrievedProduct(3L, "Middle score", 0.5D);

        final List<RetrievedProduct> products = List.of(lowest, highest, middle).stream()
                .sorted()
                .toList();

        assertEquals(List.of(highest, middle, lowest), products);
    }

    @Test
    void retainsTheProductWithTheHigherScore() {
        final RetrievedProduct lowerScore = new RetrievedProduct(1L, "Lower score", 0.2D);
        final RetrievedProduct higherScore = new RetrievedProduct(1L, "Higher score", 0.9D);

        assertSame(higherScore, lowerScore.betterOf(higherScore));
        assertSame(higherScore, higherScore.betterOf(lowerScore));
    }

    @Test
    void rejectsProductsWithDifferentIds() {
        final RetrievedProduct firstProduct = new RetrievedProduct(1L, "First product", 0.9D);
        final RetrievedProduct secondProduct = new RetrievedProduct(2L, "Second product", 0.8D);

        assertThrows(IllegalArgumentException.class, () -> firstProduct.betterOf(secondProduct));
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsNonFiniteScores(final double score) {
        assertThrows(IllegalArgumentException.class, () -> new RetrievedProduct(1L, "Product", score));
    }
}
