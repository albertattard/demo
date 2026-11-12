package demo;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static demo.utils.Logs.log;

public final class ConcurrentHashMapWordCount implements WordCount {

    private final Map<String, Integer> map = new ConcurrentHashMap<>();

    @Override
    public void merge(final String word) {
        log("Merging word " + word);
        map.merge(word, 1, Integer::sum);
    }

    @Override
    public boolean match(final Map<String, Integer> other) {
        return Objects.equals(map, other);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        final ConcurrentHashMapWordCount other = (ConcurrentHashMapWordCount) object;
        return Objects.equals(map, other.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
