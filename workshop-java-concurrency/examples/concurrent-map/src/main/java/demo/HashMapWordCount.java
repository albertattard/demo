package demo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static demo.utils.Logs.log;

public final class HashMapWordCount implements WordCount {

    /* Protected by this object's monitor (synchronized) */
    private final Map<String, Integer> map = new HashMap<>();

    @Override
    public synchronized void merge(final String word) {
        log("Merging word " + word);
        map.merge(word, 1, Integer::sum);
    }

    @Override
    public synchronized boolean match(final Map<String, Integer> other) {
        return Objects.equals(map, other);
    }

    @Override
    public synchronized boolean equals(final Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        final HashMapWordCount other = (HashMapWordCount) object;
        return Objects.equals(map, other.map);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(map);
    }

    @Override
    public synchronized String toString() {
        return map.toString();
    }
}
