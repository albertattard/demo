package demo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class Main {

    public static void main(final String[] args) {

        final Set<String> set = Set.of("Head First Java", "Java: A Beginner's Guide", "Java for Dummies",
                "Effective Java", "Head First Design Patterns", "Spring in Action", "Clean Code",
                "Test Driven: TDD and Acceptance TDD for Java Developers", "Test-Driven Java Development",
                "Thinking in Java", "Core Java Volume I - Fundamentals", "Java - The Complete Reference",
                "The Java Module System");
        System.out.println("Number of books in the set " + set.size());

        final Map<Book, Integer> map = new HashMap<>();
        for (int i = 0; i < 10_000_000; i++) {
            for (final String name : set) {
                map.putIfAbsent(new Book(name), 1);
            }
        }

        System.out.println("Number of books in the map " + map.size());
    }
}
