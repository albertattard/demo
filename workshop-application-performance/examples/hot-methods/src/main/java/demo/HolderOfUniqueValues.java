package demo;

import java.util.Collection;
import java.util.LinkedList;

public final class HolderOfUniqueValues {

    private final Collection<Integer> collection;

    public HolderOfUniqueValues() {
        /* Hint: This collection will only contain unique elements! */
        collection = new LinkedList<>();
    }

    public void initialize(final int moduloDivisor) {
        collection.clear();
        for (int i = 1; i < 10_000; i++) {
            if ((i % moduloDivisor) != 0) {
                collection.add(i);
            }
        }
    }

    public int countIntersection(final HolderOfUniqueValues other) {
        int count = 0;
        for (final Integer i : collection) {
            if (other.collection.contains(i)) {
                count++;
            }
        }
        return count;
    }
}
