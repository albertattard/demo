package demo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static demo.utils.Logs.log;

public final class SynchronizedMapWordCount implements WordCount {

    private final Map<String, Integer> map = Collections.synchronizedMap(new HashMap<>());

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
        final SynchronizedMapWordCount other = (SynchronizedMapWordCount) object;
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
