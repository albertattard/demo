package demo;

import java.util.Map;

public interface WordCount {
    void merge(String word);

    boolean match(Map<String, Integer> other);
}
